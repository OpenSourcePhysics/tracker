package test;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import com.avpkit.core.IError;
// The correct, verified avpkit-core imports
import com.avpkit.mediatool.IMediaReader;
import com.avpkit.mediatool.ToolFactory;
import com.avpkit.mediatool.MediaListenerAdapter;
import com.avpkit.mediatool.event.IVideoPictureEvent;

public class SimpleAVPVideoPlayer extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private VideoPanel videoPanel;
    private String savedVideoPath;
    private IMediaReader reader;
    
    // UI Elements for progress
    private JSlider frameSlider;
    private JLabel frameLabel;
    private JMenu fileMenu;
    private JMenuItem openItem;
    
    // Playback state control flags
    private volatile boolean isPlaying = false;
    private volatile boolean isStepRequested = false;
    private volatile boolean hasDecodedFrame = false;
    private volatile boolean isRunning = true;
    private volatile int currentFrameCount = 0;
    private int initialSliderMax = 1000;
    
    // Framerate synchronization variables
    private volatile boolean resetSync = true;
    private long playStartSystemTimeMs = 0;
    private long playStartVideoTimeMs = 0;

    public SimpleAVPVideoPlayer() {
        setTitle("AVPKit Video Player Controls Demo");
        setSize(800, 680); // Increased height to fit the slider panel
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        
        videoPanel = new VideoPanel();
        add(videoPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        setJMenuBar(createJMenuBar());
       
        setVisible(true);
        startPlaybackLoop();
    }

    private JPanel createBottomPanel() {
        JPanel mainBottomPanel = new JPanel(new BorderLayout());

        // --- SLIDER PANEL ---
        JPanel sliderPanel = new JPanel(new BorderLayout(10, 0));
        frameLabel = new JLabel("Frame: 0");
        
        // Placeholder max. True duration determined when EOF reached.
        frameSlider = new JSlider(0, initialSliderMax, 0); 
        frameSlider.setEnabled(false); // View-only since seeking isn't implemented
        
        sliderPanel.add(frameLabel, BorderLayout.WEST);
        sliderPanel.add(frameSlider, BorderLayout.CENTER);
        
        // --- BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnPlay = new JButton("Play");
        JButton btnStop = new JButton("Stop");
        JButton btnStep = new JButton("Single Step");
        JButton btnReset = new JButton("Reset");

        btnPlay.addActionListener(e -> {
            if (!isPlaying) {
            		if (currentFrameCount>frameSlider.getMaximum()) {
            			resetVideo();
            		}
                resetSync = true;
                isPlaying = true;
            }
        });
        
        btnStop.addActionListener(e -> {
            isPlaying = false;
            resetSync = true;
        });
        
        btnStep.addActionListener(e -> {
	      		if (currentFrameCount>frameSlider.getMaximum()) {
	      			resetVideo();
	      		}
            isPlaying = false;
            resetSync = true; 
            isStepRequested = true;
        });
        
        btnReset.addActionListener(e -> resetVideo());

        buttonPanel.add(btnPlay);
        buttonPanel.add(btnStop);
        buttonPanel.add(btnStep);
        buttonPanel.add(btnReset);

        // Combine them
        mainBottomPanel.add(sliderPanel, BorderLayout.NORTH);
        mainBottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainBottomPanel;
    }
    
    private JMenuBar createJMenuBar() {
    	JMenuBar menubar = new JMenuBar();
      fileMenu = new JMenu("File");      
      menubar.add(fileMenu);
      openItem = new JMenuItem("Open...");
      fileMenu.add(openItem);
			openItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					int result = fc.showOpenDialog(SimpleAVPVideoPlayer.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						if (file != null) {
	          	String path = file.getAbsolutePath();
	          	loadVideo(path);
						}
					}

				}
			});

    	return menubar;
    }


    public synchronized void loadVideo(String videoPath) {
        this.savedVideoPath = videoPath;
        isStepRequested = true; // to show first frame
        initReader();
    }

    private synchronized void initReader() {
        if (savedVideoPath == null) return;
        
        reader = ToolFactory.makeReader(savedVideoPath);
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

        reader.addListener(new MediaListenerAdapter() {
            @Override
            public void onVideoPicture(IVideoPictureEvent event) {
                BufferedImage frame = event.getImage();
                if (frame != null) {
                    
                    // --- FRAMERATE SYNCHRONIZATION ---
                    long currentVideoTimeMs = event.getTimeStamp() / 1000;
                    
                    if (resetSync) {
                        playStartSystemTimeMs = System.currentTimeMillis();
                        playStartVideoTimeMs = currentVideoTimeMs;
                        resetSync = false;
                    } else if (isPlaying) {
                        long expectedSystemTimeMs = playStartSystemTimeMs + (currentVideoTimeMs - playStartVideoTimeMs);
                        long sleepTimeMs = expectedSystemTimeMs - System.currentTimeMillis();
                        
                        if (sleepTimeMs > 0) {
                            try {
                                Thread.sleep(sleepTimeMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                    // ---------------------------------

                    currentFrameCount++;
                    
                    // Safely update the Swing UI from this background thread
                    SwingUtilities.invokeLater(() -> {
                        frameLabel.setText("Frame: " + (currentFrameCount-1));
                        frameSlider.setValue(currentFrameCount-1);
                    });

                    videoPanel.updateFrame(frame);
                    hasDecodedFrame = true;
                }
            }
        });
    }

    private void startPlaybackLoop() {
        new Thread(() -> {
            try {
                while (isRunning) {
                    if (reader != null && isPlaying) {
                    		Object result = reader.readPacket();
                        if (result != null) {
                            isPlaying = false;
                            IError err = (IError)result;
                            if (err.getType() == com.avpkit.core.IError.Type.ERROR_EOF) {
		                            if (frameSlider.getMaximum()==initialSliderMax)
		                            	frameSlider.setMaximum(currentFrameCount-1);
		                            else isStepRequested = false;
                            }
                        }
                    } else if (reader != null && isStepRequested) {
                        hasDecodedFrame = false;
                        while (!hasDecodedFrame && isRunning) {
                      		Object result = reader.readPacket();
                          if (result != null) {
                              isPlaying = false;
                              IError err = (IError)result;
                              if (err.getType() == com.avpkit.core.IError.Type.ERROR_EOF) {
		                            if (frameSlider.getMaximum()==initialSliderMax)
		                            	frameSlider.setMaximum(currentFrameCount-1);
                              }
                              break;
                          }
                        }
                        isStepRequested = false;
                    } else {
                        Thread.sleep(15); 
                    }
                }
            } catch (Exception e) {
                System.err.println("Playback processing loop error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                cleanupReader();
            }
        }).start();
    }

    private synchronized void resetVideo() {
        isPlaying = false;
        resetSync = true;
        currentFrameCount = 0;
        
        // Reset the UI graphics
        SwingUtilities.invokeLater(() -> {
            frameLabel.setText("Frame: 0");
            frameSlider.setValue(0);
        });
        
        cleanupReader();
        isStepRequested = true; // to show first frame
        initReader();
    }

    private synchronized void cleanupReader() {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    private class VideoPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private BufferedImage currentFrame;

        public synchronized void updateFrame(BufferedImage frame) {
            this.currentFrame = frame;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            synchronized (this) {
                if (currentFrame != null) {
                    g.drawImage(currentFrame, 0, 0, getWidth(), getHeight(), null);
                }
            }
        }
    }

    public static void main(String[] args) {
      SimpleAVPVideoPlayer player = new SimpleAVPVideoPlayer();
    }
}