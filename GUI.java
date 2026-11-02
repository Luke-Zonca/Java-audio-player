import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Dark-themed Swing front end for the audio player. All of the visual work
 * lives here; it only uses the public methods that Playlist, Song and
 * DoublyLinkedSongList already have.
 */
public class GUI extends JFrame{

    // ---- colour palette ----
    private static final Color BG        = new Color(0x121212);
    private static final Color SURFACE   = new Color(0x1E1E1E);
    private static final Color SURFACE_2 = new Color(0x2A2A2A);
    private static final Color ACCENT    = new Color(0x1DB954);
    private static final Color TEXT      = new Color(0xFFFFFF);
    private static final Color MUTED     = new Color(0xB3B3B3);
    private static final Color DANGER    = new Color(0xE5534B);

    private final Playlist playlist;
    private final DefaultListModel<Song> model = new DefaultListModel<>();
    private final JList<Song> list = new JList<>(model);

    private final JLabel nowTitle = new JLabel("Nothing playing");
    private final JLabel nowSub = new JLabel("Select a song and press Play");
    private final JLabel statsLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar loadingBar = new JProgressBar();
    private JButton addBtn;
    private JButton removeBtn;

    private boolean updating = false;   // stops list-selection events from feeding back into themselves
    private int playingIndex = -1;      // row that is actually playing right now (-1 = none)

    public static void main(String[] args){
        String csvFile = args.length > 0 ? args[0] : "playlist.csv";
        try{
            // cross-platform look so our custom colours show up the same on Mac, Windows and Linux
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e){
            // fall back to the default look
        }
        SwingUtilities.invokeLater(() -> new GUI(csvFile).setVisible(true));
    }

    public GUI(String csvFile){
        super("Audio Player");
        playlist = loadPlaylistFromCSV(csvFile);
        buildUI();
        refreshList();

        setSize(900, 620);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // =====================================================================
    //  UI construction
    // =====================================================================

    private void buildUI(){
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setContentPane(root);

        // ---- header ----
        JLabel title = new JLabel("\u266A  Audio Player");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT);

        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statsLabel.setForeground(MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(statsLabel, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ---- "now playing" card ----
        nowTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        nowTitle.setForeground(TEXT);
        nowSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nowSub.setForeground(MUTED);

        JPanel cardText = new JPanel(new GridLayout(2, 1, 0, 4));
        cardText.setOpaque(false);
        cardText.add(nowTitle);
        cardText.add(nowSub);

        RoundPanel nowCard = new RoundPanel(SURFACE, 20);
        nowCard.setLayout(new BorderLayout());
        nowCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        nowCard.add(cardText, BorderLayout.CENTER);

        // ---- song list ----
        list.setBackground(SURFACE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new SongRenderer());
        list.setFixedCellHeight(56);

        // single click = select, double click = play
        list.addListSelectionListener(e -> {
            if(updating || e.getValueIsAdjusting()){
                return;
            }
            int idx = list.getSelectedIndex();
            if(idx >= 0){
                playlist.setCurrentSong(idx);
                list.repaint();
            }
        });
        list.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount() != 2){
                    return;
                }
                int idx = list.locationToIndex(e.getPoint());
                Rectangle bounds = idx >= 0 ? list.getCellBounds(idx, idx) : null;
                if(bounds == null || !bounds.contains(e.getPoint())){
                    return; // clicked the empty space below the last song
                }
                playlist.setCurrentSong(idx);
                play();
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(SURFACE_2));
        scroll.getViewport().setBackground(SURFACE);
        JScrollBar vbar = scroll.getVerticalScrollBar();
        vbar.setUI(new SlimScrollBarUI());
        vbar.setPreferredSize(new Dimension(12, 0));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(nowCard, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        // thin animated bar that only shows while something is loading
        loadingBar.setIndeterminate(true);
        loadingBar.setBorderPainted(false);
        loadingBar.setForeground(ACCENT);
        loadingBar.setBackground(SURFACE_2);
        loadingBar.setPreferredSize(new Dimension(160, 6));
        loadingBar.setVisible(false);

        JPanel loadingWrap = new JPanel(new GridBagLayout()); // keeps the bar thin and vertically centered
        loadingWrap.setOpaque(false);
        loadingWrap.add(loadingBar);

        JPanel statusRow = new JPanel(new BorderLayout(10, 0));
        statusRow.setOpaque(false);
        statusRow.add(statusLabel, BorderLayout.CENTER);
        statusRow.add(loadingWrap, BorderLayout.EAST);
        center.add(statusRow, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        // ---- controls ----
        FlatButton prev   = new FlatButton("\u25C0  Prev",  SURFACE_2, TEXT);
        FlatButton play   = new FlatButton("\u25B6  Play",  ACCENT, Color.BLACK);
        FlatButton stop   = new FlatButton("\u25A0  Stop",  SURFACE_2, TEXT);
        FlatButton next   = new FlatButton("Next  \u25B6",  SURFACE_2, TEXT);
        FlatButton add    = new FlatButton("+  Add song",   SURFACE_2, TEXT);
        FlatButton remove = new FlatButton("Remove",        SURFACE_2, DANGER);

        prev.addActionListener(e -> playPrevious());
        play.addActionListener(e -> play());
        stop.addActionListener(e -> stop());
        next.addActionListener(e -> playNext());
        add.addActionListener(e -> addSong());
        remove.addActionListener(e -> removeSong());
        addBtn = add;
        removeBtn = remove;

        JPanel transport = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        transport.setOpaque(false);
        transport.add(prev);
        transport.add(play);
        transport.add(stop);
        transport.add(next);

        JPanel manage = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        manage.setOpaque(false);
        manage.add(add);
        manage.add(remove);

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        controls.add(transport, BorderLayout.CENTER);
        controls.add(manage, BorderLayout.EAST);
        root.add(controls, BorderLayout.SOUTH);
    }

    // =====================================================================
    //  Button actions
    // =====================================================================

    private void play(){
        showResult(playlist.playCurrentSong());
    }

    private void stop(){
        playlist.stopPlayback();
        playingIndex = -1;
        Song s = playlist.getCurrentSong();
        nowTitle.setText(s != null ? s.getTitle() : "Nothing playing");
        nowSub.setText(s != null ? s.getArtist() + "  \u2022  Stopped" : "Select a song and press Play");
        showStatus(" ");
        list.repaint();
    }

    private void playNext(){
        if(!playlist.hasSongs()){
            showStatus("There are no songs");
        } else if(!playlist.hasNext()){
            showStatus("Reached the end of the playlist");
        } else{
            showResult(playlist.playNext());
        }
    }

    private void playPrevious(){
        // checked here so we never step before the first song
        if(!playlist.hasSongs()){
            showStatus("There are no songs");
        } else if(!playlist.hasPrevious()){
            showStatus("This is the start of the playlist");
        } else{
            showResult(playlist.playPrevious());
        }
    }

    private void removeSong(){
        String msg = playlist.removeCurrentSong(); // also stops playback
        playingIndex = -1;
        nowTitle.setText("Nothing playing");
        nowSub.setText("Select a song and press Play");
        refreshList();
        showStatus(msg);
    }

    private void addSong(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "WAV or AIFF audio", "wav", "aif", "aiff"));
        if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION){
            return; // user cancelled
        }
        File file = chooser.getSelectedFile();

        JTextField titleField = new JTextField(file.getName().replaceFirst("\\.[^.]+$", ""));
        JTextField artistField = new JTextField("Unknown");
        Object[] fields = {"Title:", titleField, "Artist:", artistField};
        int result = JOptionPane.showConfirmDialog(this, fields, "Add song",
            JOptionPane.OK_CANCEL_OPTION);
        if(result != JOptionPane.OK_OPTION){
            return;
        }

        String title = titleField.getText().trim();
        String artist = artistField.getText().trim();
        if(title.isEmpty()){
            showStatus("Song needs a title");
            return;
        }

        // Reading the file can take a moment, so do it off the UI thread
        // and show a loading indicator until it finishes.
        setBusy(true, "Loading \"" + file.getName() + "\"...");
        new SwingWorker<Integer, Void>(){
            @Override
            protected Integer doInBackground(){
                return getDurationSeconds(file);
            }

            @Override
            protected void done(){
                setBusy(false, " ");
                int duration;
                try{
                    duration = get();
                } catch (Exception ex){
                    duration = -1;
                }
                if(duration < 0){
                    showStatus("Could not read that file. Only WAV and AIFF are supported.");
                    return;
                }
                playlist.addSong(new Song(title, artist, file.getAbsolutePath(), duration));
                refreshList();
                showStatus("Added: " + title);
            }
        }.execute();
    }

    // =====================================================================
    //  Helpers that update what's on screen
    // =====================================================================

    /** Updates the now-playing card and list after a play attempt. */
    private void showResult(String msg){
        Song s = playlist.getCurrentSong();
        if(msg.startsWith("Now playing") && s != null){
            playingIndex = playlist.getCurrentIndex();
            nowTitle.setText(s.getTitle());
            nowSub.setText(s.getArtist() + "  \u2022  Playing");
            showStatus(" ");
        } else{
            playingIndex = -1;
            nowTitle.setText("Nothing playing");
            nowSub.setText("Select a song and press Play");
            showStatus(msg);
        }
        refreshList();
    }

    private void showStatus(String msg){
        statusLabel.setText(msg);
    }

    /** Shows or hides the loading indicator and locks Add/Remove while it's up. */
    private void setBusy(boolean busy, String msg){
        loadingBar.setVisible(busy);
        addBtn.setEnabled(!busy);
        removeBtn.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        showStatus(msg);
    }

    /** Rebuilds the list from the playlist and re-selects the current song. */
    private void refreshList(){
        updating = true;
        model.clear();
        int totalSec = 0;
        DoublyLinkedSongNode curr = playlist.getLL().getHead();
        while(curr != null){
            Song s = curr.getSong();
            model.addElement(s);
            totalSec += s.getDuration();
            curr = curr.getNext();
        }
        int idx = playlist.getCurrentIndex();
        if(idx >= 0 && idx < model.size()){
            list.setSelectedIndex(idx);
            list.ensureIndexIsVisible(idx);
        } else{
            list.clearSelection();
        }
        updating = false;

        statsLabel.setText(model.size() + (model.size() == 1 ? " song" : " songs")
            + "   \u2022   " + formatTime(totalSec));
        list.repaint();
    }

    private static String formatTime(int totalSeconds){
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    // =====================================================================
    //  Custom-painted components
    // =====================================================================

    /** One row of the song list: number, title/artist, duration. */
    private class SongRenderer extends JPanel implements ListCellRenderer<Song>{
        private final JLabel num = new JLabel("", SwingConstants.CENTER);
        private final JLabel titleLabel = new JLabel();
        private final JLabel artistLabel = new JLabel();
        private final JLabel dur = new JLabel();

        SongRenderer(){
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            num.setPreferredSize(new Dimension(28, 0));
            num.setFont(new Font("SansSerif", Font.PLAIN, 14));
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            artistLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            artistLabel.setForeground(MUTED);
            dur.setFont(new Font("SansSerif", Font.PLAIN, 13));
            dur.setForeground(MUTED);

            JPanel text = new JPanel(new GridLayout(2, 1));
            text.setOpaque(false);
            text.add(titleLabel);
            text.add(artistLabel);

            add(num, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(dur, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Song> l, Song song,
                int index, boolean isSelected, boolean hasFocus){
            boolean playingRow = index == playingIndex;

            setBackground(isSelected ? SURFACE_2 : SURFACE);
            num.setText(playingRow ? "\u25B6" : String.valueOf(index + 1));
            num.setForeground(playingRow ? ACCENT : MUTED);
            titleLabel.setText(song.getTitle());
            titleLabel.setForeground(playingRow ? ACCENT : TEXT);
            artistLabel.setText(song.getArtist());
            dur.setText(formatTime(song.getDuration()));
            return this;
        }
    }

    /** A panel with a rounded, filled background. */
    private static class RoundPanel extends JPanel{
        private final Color fill;
        private final int arc;

        RoundPanel(Color fill, int arc){
            this.fill = fill;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A pill-shaped flat button with a hover highlight. */
    private static class FlatButton extends JButton{
        private final Color base;
        private final Color hover;
        private boolean over = false;

        FlatButton(String text, Color base, Color fg){
            super(text);
            this.base = base;
            this.hover = base.brighter();
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(fg);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e){ over = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(over ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g); // draws the text on top
        }
    }

    /** A thin, dark scrollbar with no arrow buttons. */
    private static class SlimScrollBarUI extends BasicScrollBarUI{
        @Override
        protected void configureScrollBarColors(){
            thumbColor = new Color(0x535353);
            trackColor = SURFACE;
        }
        @Override
        protected JButton createDecreaseButton(int orientation){
            return zeroButton();
        }
        @Override
        protected JButton createIncreaseButton(int orientation){
            return zeroButton();
        }
        private JButton zeroButton(){
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r){
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // =====================================================================
    //  CSV loading and file helpers (unchanged logic)
    // =====================================================================

    /**
     * Reads a playlist in from a CSV file with the header
     * title,artist,duration,genre,filePath and builds a Playlist out
     * of its rows. A missing file, a malformed row, or a bad duration
     * value are all reported and skipped rather than crashing the
     * loader.
     *
     * @param filename path to the CSV file
     * @return the Playlist built from the file (possibly empty)
     */
    public static Playlist loadPlaylistFromCSV(String filename){
        Playlist newPlay = new Playlist("playlist");

        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line = br.readLine(); //header row, skip it
            while((line = br.readLine()) != null){
                if(line.trim().isEmpty()){
                    continue;
                }
                String[] parts = line.split(",");
                if(parts.length < 5){
                    System.out.println("Skipping malformed row: " + line);
                    continue;
                }

                String title = stripQuotes(parts[0]);
                String artist = stripQuotes(parts[1]);
                int duration;
                try{
                    duration = Integer.parseInt(stripQuotes(parts[2]));
                } catch (NumberFormatException e){
                    System.out.println("Skipping row with bad duration: " + line);
                    continue;
                }
                //parts[3] is genre - Song doesn't track genre, so it's read but not stored
                String filePath = stripQuotes(parts[4]);

                Song oneSong = new Song(title, artist, filePath, duration);
                DoublyLinkedSongNode oneSongNode = new DoublyLinkedSongNode(oneSong);
                newPlay.addSong(oneSongNode);
            }
        } catch (FileNotFoundException e){
            System.out.println("File not found: " + filename);
        } catch (IOException e){
            e.printStackTrace();
        }
        return newPlay;
    }

    /**
     * Strips one pair of surrounding double quotes off a CSV field,
     * if the field has them.
     */
    private static String stripQuotes(String field){
        String trimmed = field.trim();
        if(trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")){
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * gets the duration of a selected song so that the user dosent need to enter that information
     * @param file takes in the file from the file selecter
     * @return returns an int of how long the song is, or -1 if it can't be read
     */
    private static int getDurationSeconds(File file){
        try(javax.sound.sampled.AudioInputStream in =
                javax.sound.sampled.AudioSystem.getAudioInputStream(file)){
            javax.sound.sampled.AudioFormat format = in.getFormat();
            long frames = in.getFrameLength();
            if(frames < 0){
                return 0; // length unknown
            }
            return (int) (frames / format.getFrameRate());
        } catch (Exception ex){
            return -1; // unreadable or unsupported
        }
    }
}
