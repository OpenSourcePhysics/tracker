package org.opensourcephysics.cabrillo.tracker;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.numerics.Util;
import org.opensourcephysics.tools.FontSizer;

@SuppressWarnings("serial")
public class DataTrackClipControl extends JPanel implements PropertyChangeListener {
	
	static Color videoColor=Color.WHITE;
	static Color dataColor=videoColor;
	static Color dataClipColor=new Color(51, 200, 51);
	static Color unavailableDataColor=new Color(51, 200, 51, 127);
	static Color availableDataColor=unavailableDataColor;
	static int graphicHeight = 100;
	
	protected DataTrack dataTrack;
	protected DrawingPanel drawingPanel;
	protected JPanel spinnerPanel;
	protected Interactive mappingGraphic;
	protected JLabel videoInLabel, dataInLabel, dataClipLengthLabel, dataStrideLabel;
	protected JSpinner videoInSpinner, dataInSpinner, dataClipLengthSpinner, dataStrideSpinner;
	protected boolean refreshing, drawVideoClip=false;
	protected ParticleDataTrack particleDT;

	/**
	 * Constructor.
	 * 
	 * @param model the DataTrack
	 */
	public DataTrackClipControl(DataTrack model) {
		super(new BorderLayout());
		dataTrack = model;
		if (dataTrack instanceof ParticleDataTrack) {
			particleDT = (ParticleDataTrack)dataTrack;
			particleDT.addPropertyChangeListener(
					ParticleDataTrack.PROPERTY_PARTICLEDATATRACK_DATACLIP, this);
			particleDT.addPropertyChangeListener(
					VideoPlayer.PROPERTY_VIDEOPLAYER_VIDEOCLIP, this);
		}
		createGUI();
		refreshSpinners();
		refreshGUI();
	}
	
	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		// create labels
		videoInLabel = new JLabel();
		videoInLabel.setBorder(BorderFactory.createEmptyBorder());
		dataInLabel = new JLabel();
		dataInLabel.setBorder(BorderFactory.createEmptyBorder());
		dataClipLengthLabel = new JLabel();
		dataClipLengthLabel.setBorder(BorderFactory.createEmptyBorder());
		dataStrideLabel = new JLabel();
		dataStrideLabel.setBorder(BorderFactory.createEmptyBorder());

		// create spinners
		videoInSpinner = new MySpinner(new SpinnerNumberModel(0, 0, 20, 1));
		videoInSpinner.addChangeListener((e) -> {
			if (refreshing)
				return;
	  	VideoPanel vidPanel = dataTrack.getVideoPanel();
	  	if (vidPanel == null)
	  		return;
  		VideoClip clip = vidPanel.getPlayer().getVideoClip();
  		int cur = clip.frameToStep(dataTrack.getStartFrame());
			int in = (Integer) videoInSpinner.getValue();
			if (in == cur) {
				return;
			}
//			dataTrack.setStartFrame(in);
			dataTrack.setStartStep(in);
			
			TFrame.repaintT(DataTrackClipControl.this);
			videoInSpinner.requestFocusInWindow();
		});
		dataInSpinner = new MySpinner(new SpinnerNumberModel(0, 0, 20, 1));
		dataInSpinner.addChangeListener((e) -> {
			if (refreshing)
				return;
			int in = (Integer) dataInSpinner.getValue();
			if (in == dataTrack.getDataClip().getStartIndex()) {
				return;
			}
			dataTrack.getDataClip().setStartIndex(in);
			dataInSpinner.setValue(dataTrack.getDataClip().getStartIndex());
			dataInSpinner.requestFocusInWindow();
			TFrame.repaintT(DataTrackClipControl.this);
		});

		dataClipLengthSpinner = new MySpinner(new SpinnerNumberModel(1, 1, 20, 1));

		dataClipLengthSpinner.addChangeListener((e) -> {
			if (refreshing)
				return;
			int length = (Integer) dataClipLengthSpinner.getValue();
			if (length == dataTrack.getDataClip().getClipLength()) {
				return;
			}
			dataTrack.getDataClip().setClipLength(length);
			dataClipLengthSpinner.setValue(dataTrack.getDataClip().getClipLength());
			TFrame.repaintT(DataTrackClipControl.this);
			dataClipLengthSpinner.requestFocusInWindow();
			if (particleDT != null && particleDT.tp != null)
				particleDT.tp.getModelBuilder().refreshSpinners();
				
		});

		dataStrideSpinner = new MySpinner(new SpinnerNumberModel(1, 1, 10, 1));
		dataStrideSpinner.addChangeListener((e) -> {
			if (refreshing)
				return;
			int n = (Integer) dataStrideSpinner.getValue();
			if (n == dataTrack.getDataClip().getStride()) {
				return;
			}
			dataTrack.getDataClip().setStride(n);
			dataStrideSpinner.setValue(dataTrack.getDataClip().getStride());
			TFrame.repaintT(DataTrackClipControl.this);
			dataStrideSpinner.requestFocusInWindow();
		});

		// assemble
		drawingPanel = new GraphicPanel();
		drawingPanel.setBorder(BorderFactory.createEtchedBorder());
		drawingPanel.addDrawable(new MappingGraphic());
		add(drawingPanel, BorderLayout.CENTER);
		spinnerPanel = new JPanel(new GridLayout(1, 4));
		add(spinnerPanel, BorderLayout.SOUTH);

		JPanel singleSpinnerPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel();
		panel.add(videoInSpinner);
		singleSpinnerPanel.add(panel, BorderLayout.NORTH);
		panel = new JPanel();
		panel.add(videoInLabel);
		singleSpinnerPanel.add(panel, BorderLayout.SOUTH);
		spinnerPanel.add(singleSpinnerPanel);

		singleSpinnerPanel = new JPanel(new BorderLayout());
		panel = new JPanel();
		panel.add(dataClipLengthSpinner);
		singleSpinnerPanel.add(panel, BorderLayout.NORTH);
		panel = new JPanel();
		panel.add(dataClipLengthLabel);
		singleSpinnerPanel.add(panel, BorderLayout.SOUTH);
		spinnerPanel.add(singleSpinnerPanel);

		singleSpinnerPanel = new JPanel(new BorderLayout());
		panel = new JPanel();
		panel.add(dataInSpinner);
		singleSpinnerPanel.add(panel, BorderLayout.NORTH);
		panel = new JPanel();
		panel.add(dataInLabel);
		singleSpinnerPanel.add(panel, BorderLayout.SOUTH);
		spinnerPanel.add(singleSpinnerPanel);

		singleSpinnerPanel = new JPanel(new BorderLayout());
		panel = new JPanel();
		panel.add(dataStrideSpinner);
		singleSpinnerPanel.add(panel, BorderLayout.NORTH);
		panel = new JPanel();
		panel.add(dataStrideLabel);
		singleSpinnerPanel.add(panel, BorderLayout.SOUTH);
		spinnerPanel.add(singleSpinnerPanel);

	}
	
	/**
	 * Refreshes the spinners.
	 */
	protected void refreshSpinners() {
		VideoPanel vidPanel = dataTrack.getVideoPanel();
		if (vidPanel == null)
			return;

		DataClip dataClip = dataTrack.getDataClip();
		VideoClip videoClip = vidPanel.getPlayer().getVideoClip();

		// data start index
		int clipLength = dataClip.getClipLength();
		int dataLength = dataClip.getDataLength();
		int max = Math.max(0, dataLength - 1);
		Util.newSpinnerNumberModel(dataInSpinner, dataClip.getStartIndex(), 0, max, 1);
		// data stride
		max = Math.max(1, dataLength - 1);
		max = Math.max(max, dataClip.getStride());
		Util.newSpinnerNumberModel(dataStrideSpinner, dataClip.getStride(), 1, max, 1);
		if (videoClip != null) {
			// video start frame
//			int first = videoClip.getFirstFrameNumber();
			int first = 0;
//			int last = videoClip.getLastFrameNumber();
			int last = videoClip.getStepCount() - 1;
//			int startStep = videoClip.frameToStep(dataTrack.getStartFrame());
			int startStep = dataTrack.getStartStep();

			startStep = Math.max(startStep, first);
			startStep = Math.min(startStep, last);
			Util.newSpinnerNumberModel(videoInSpinner, startStep, first, last, 1);
		}
		// step count (clip length)
		max = Math.max(1, dataLength);
		Util.newSpinnerNumberModel(dataClipLengthSpinner, clipLength, 1, max, 1);
		Container c = this.getTopLevelAncestor();
		if (c instanceof ModelBuilder) {
			((ModelBuilder) c).refreshSpinners();
		}
	}

	/**
	 * Refreshes the GUI.
	 */
	protected void refreshGUI() {
		setBorder(BorderFactory.createTitledBorder(TrackerRes.getString("DataTrackClipControl.Border.Title"))); //$NON-NLS-1$
  	videoInLabel.setText(TrackerRes.getString("DataTrackClipControl.Label.VideoStart")); //$NON-NLS-1$
  	dataClipLengthLabel.setText(TrackerRes.getString("DataTrackClipControl.Label.FrameCount")); //$NON-NLS-1$
  	dataInLabel.setText(TrackerRes.getString("DataTrackClipControl.Label.DataStart")); //$NON-NLS-1$
  	dataStrideLabel.setText(TrackerRes.getString("DataTrackClipControl.Label.Stride")); //$NON-NLS-1$
	}
	
	/**
	 * Adds a mouse listener to all JPanels associated with this control.
	 */
	protected void addMouseListenerToAll(MouseListener listener) {
		this.addMouseListener(listener);
		drawingPanel.addMouseListener(listener);
		spinnerPanel.addMouseListener(listener);
	}

  @Override
  public Dimension getMaximumSize() {
  	Dimension dim = super.getMaximumSize();
  	dim.height = getPreferredSize().height;
    return dim;
  }

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		refreshSpinners();
		TFrame.repaintT(DataTrackClipControl.this);
	}
  
  /**
   * Gets the last displayed clip index. An index is displayed if its
   * corresponding frame is less than or equal to the video's last frame number. 
   *
   * @return the index
   */
  private int getLastDisplayedClipIndex() {
  	int stepCount = dataTrack.getDataClip().getClipLength();
  	VideoPanel vidPanel = dataTrack.getVideoPanel();
  	if (vidPanel==null) return stepCount;
  	
    VideoClip clip = vidPanel.getPlayer().getVideoClip();
    int last = clip.getLastFrameNumber();
    for (int i = stepCount-1; i>0; i--) {
    	// determine corresponding frame number and index
    	int frame = dataTrack.getStartFrame()+(i * clip.getStepSize());
    	int index = dataTrack.getDataClip().stepToIndex(i);
    	// look for first step with frame<=last frame and index<data length
      if (frame<=last && index<dataTrack.getDataClip().getDataLength()) {
      	// return the index
        return index;
      }
    }
    return dataTrack.getDataClip().stepToIndex(0);
  }
  
  /**
   * Sets the interactive graphic element that displays data elements and video frames. 
   *
   * @param graphic the graphic element
   */
  public void setGraphic(Interactive graphic) {
  	if (graphic==null) return;
  	if (mappingGraphic!=null) {
    	drawingPanel.removeDrawable(mappingGraphic);
  	}
  	mappingGraphic = graphic;
  	drawingPanel.addDrawable(mappingGraphic);
  }
  
  /**
   * An Interactive that displays and maps data elements to video frames. 
   */
  class MappingGraphic implements Interactive {
  	
		GeneralPath path = new GeneralPath();

		@Override
		public void draw(DrawingPanel panel, Graphics g) {
	    VideoPanel vidPanel = dataTrack.getVideoPanel();
	    if (vidPanel==null) return;
	    
      Graphics2D g2 = (Graphics2D)g;
            
      double mag = FontSizer.getFactor();
      int strokeWidth = (int)(8 * mag);
      g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      
      // fill background
      Rectangle rect = new Rectangle(drawingPanel.getSize());
      g2.setColor(new Color(200, 200, 200));
      g2.fill(rect);
      
			
			int yClipLine = (int)(30 * mag);
      int yDataLine = (int)(70 * mag);
      int yVideoFrame = yClipLine + (int)(3*g2.getFontMetrics().getHeight()/4);
      
      // draw video and data labels
      g2.setColor(Color.DARK_GRAY);
      g2.setFont(videoInLabel.getFont());
      RenderingHints rh = g2.getRenderingHints();
      rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
			int largeGap = 10, smallGap = 4;
      int fontDrop = g2.getFontMetrics().getHeight()/4;
      String s = TrackerRes.getString("DataTrackClipControl.Label.Video"); //$NON-NLS-1$
      int labelSpace = g2.getFontMetrics().stringWidth(s);
      g2.drawString(s, largeGap, yClipLine+fontDrop);

      s = TrackerRes.getString("DataTrackClipControl.Label.Data"); //$NON-NLS-1$
      labelSpace = Math.max(labelSpace, g2.getFontMetrics().stringWidth(s));
      g2.drawString(s, largeGap, yDataLine+fontDrop);
      
      s = TrackerRes.getString("DataTrackClipControl.Label.Frame"); //$NON-NLS-1$
      labelSpace = Math.max(labelSpace, g2.getFontMetrics().stringWidth(s));
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN));
      g2.drawString(s, largeGap, yVideoFrame+fontDrop);
      
      labelSpace += 6;
      
      VideoClip videoClip = vidPanel.getPlayer().getVideoClip();
      DataClip dataClip = dataTrack.getDataClip();
      
      s = "0"; //$NON-NLS-1$
      int frontSpace = g2.getFontMetrics().stringWidth(s);

      s = String.valueOf(videoClip.getStepCount()-1);
      int endSpace = g2.getFontMetrics().stringWidth(s);
      s = String.valueOf(dataClip.getDataLength()-1);
      endSpace = Math.max(endSpace, g2.getFontMetrics().stringWidth(s));

      int videoClipFrames = videoClip.getStepCount();
      int dataClipFrames = dataClip.getDataLength();
			int maxFrames = Math.max(videoClipFrames, dataClipFrames);
			
			double maxLength = panel.getWidth()-labelSpace-frontSpace-endSpace-3*largeGap-2*smallGap;
      double lengthPerFrame = maxLength/maxFrames;
      
			// draw video line		
			double lineLength = maxLength*videoClipFrames/maxFrames;
			double leftEnd= labelSpace+2*largeGap+frontSpace;
			double rightVideoEnd = leftEnd+lineLength;
      path.reset();
      path.moveTo(leftEnd, yClipLine);
      path.lineTo(rightVideoEnd, yClipLine);
      g2.setColor(videoColor);
      g2.draw(path);
      
			// first draw video line with max "available" data clip 
      double fullClipLength = lengthPerFrame*(dataClip.getClipLength());

      int startStep = dataTrack.getStartStep();      
      double videoStartClip = leftEnd+lengthPerFrame*(startStep);

      double videoEndClip = videoStartClip + fullClipLength;
      videoStartClip = Math.max(videoStartClip, leftEnd);
      videoEndClip = Math.min(videoEndClip, leftEnd+lineLength);
      path.reset();
      path.moveTo(videoStartClip, yClipLine);
      path.lineTo(videoEndClip, yClipLine);
      g2.setColor(unavailableDataColor);
      g2.draw(path);
      
			// then draw dataclip actually used on video line
      double clipLength = lengthPerFrame*(dataClip.getAvailableClipLength());
      videoEndClip = videoStartClip + clipLength;
      videoStartClip = Math.max(videoStartClip, leftEnd);
      videoEndClip = Math.min(videoEndClip, leftEnd+lineLength);
      // adjust clip length in case video truncated it
      clipLength = videoEndClip - videoStartClip;
      path.reset();
      path.moveTo(videoStartClip, yClipLine);
      path.lineTo(videoEndClip, yClipLine);
      g2.setColor(dataClipColor);
      g2.draw(path);
      
      // draw the data line
			lineLength = maxLength*dataClipFrames/maxFrames;
			double rightDataEnd = leftEnd+lineLength;
      path.reset();
      path.moveTo(leftEnd, yDataLine);
      path.lineTo(rightDataEnd, yDataLine);
      g2.setColor(dataColor);
      g2.draw(path);
      
      // draw data clip on data line
      if (dataClip.getStride()>1) {
	      float frameWidth = Math.max(1, (float)lengthPerFrame);
      	g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,	BasicStroke.JOIN_ROUND, 8, 
      			new float[] {frameWidth, frameWidth*(dataClip.getStride()-1)}, 0));      	
      }
      double dataStartClip = leftEnd + lengthPerFrame*dataClip.getStartIndex();
      double dataEndClip = dataStartClip + fullClipLength*dataClip.getStride();
      dataEndClip = Math.min(dataEndClip, leftEnd+lineLength);
      path.reset();
      path.moveTo(dataStartClip, yDataLine);
      path.lineTo(dataEndClip, yDataLine);
      g2.setColor(availableDataColor);
      g2.draw(path);
      dataEndClip = dataStartClip + clipLength*dataClip.getStride();
      path.reset();
      path.moveTo(dataStartClip, yDataLine);
      path.lineTo(dataEndClip, yDataLine);
      g2.setColor(dataClipColor);
      g2.draw(path);

      // connect with lines
      dataEndClip = dataEndClip - (dataClip.getStride()-1)*lengthPerFrame;
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      path.reset();
      path.moveTo(dataStartClip, yDataLine-strokeWidth/2);
      path.lineTo(videoStartClip, yClipLine+strokeWidth/2);
      g2.draw(path);
      path.reset();
      path.moveTo(dataEndClip, yDataLine-strokeWidth/2);
      path.lineTo(videoEndClip, yClipLine+strokeWidth/2);
      g2.draw(path);
      
      // draw step and index numbers
      g2.setFont(g2.getFont().deriveFont(Font.BOLD));
      int verticalOffset = fontDrop;
      // zero start index for data and video clip
      s = "0"; //$NON-NLS-1$
      g2.drawString(s, (int)(leftEnd-frontSpace-smallGap), yDataLine+verticalOffset);
      g2.drawString(s, (int)(leftEnd-frontSpace-smallGap), yClipLine+verticalOffset);

      // data end index 
      s = String.valueOf(dataClipFrames-1);
      g2.drawString(s, (int)(rightDataEnd+smallGap), yDataLine+verticalOffset);
      
      // clip end step
      s = String.valueOf(videoClip.getStepCount() - 1);
      g2.drawString(s, (int)(rightVideoEnd+smallGap), yClipLine+verticalOffset);
      
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN));
      // clip start frame
      s = String.valueOf(videoClip.getStartFrameNumber());
      frontSpace = g2.getFontMetrics().stringWidth(s);
      g2.drawString(s, (int)(leftEnd-frontSpace-smallGap), yVideoFrame+verticalOffset);

      // clip end frame
      s = String.valueOf(videoClip.getEndFrameNumber());
      g2.drawString(s, (int)(rightVideoEnd+smallGap), yVideoFrame+verticalOffset);
      
      // start frame
      s = String.valueOf(dataTrack.getStartFrame());
      g2.drawString(s, (int)(videoStartClip), yVideoFrame+verticalOffset);
      
      verticalOffset = -2-strokeWidth/2;
      g2.setFont(g2.getFont().deriveFont(Font.BOLD));
      
      // start step
      s = String.valueOf(startStep);
      g2.drawString(s, (int)(videoStartClip), yClipLine+verticalOffset);
      
      // end step & frame
    	int n = startStep+dataClip.getAvailableClipLength()-1;
    	n = Math.min(n, videoClip.getStepCount()-1);
      if (startStep!=n) {
      	// end clip
	      s = String.valueOf(n);
	      int space = g2.getFontMetrics().stringWidth(s);
	      g2.drawString(s, (int)(videoEndClip-space), yClipLine+verticalOffset);
	      // end frame
	      s = String.valueOf(videoClip.stepToFrame(n));
	      g2.setFont(g2.getFont().deriveFont(Font.PLAIN));
	      space = g2.getFontMetrics().stringWidth(s);
	      verticalOffset = fontDrop;
	      g2.drawString(s, (int)(videoEndClip-space), yVideoFrame+verticalOffset);      	
      }
      
      verticalOffset = fontDrop+strokeWidth+3;
      g2.setFont(g2.getFont().deriveFont(Font.BOLD));
      s = String.valueOf(dataTrack.getDataClip().getStartIndex());
      g2.drawString(s, (int)(dataStartClip), yDataLine+verticalOffset);
    	n = getLastDisplayedClipIndex();
      if (n-dataTrack.getDataClip().getStartIndex()>0) {
	      s = String.valueOf(n);
	      int space = g2.getFontMetrics().stringWidth(s);
	      g2.drawString(s, (int)(dataEndClip-space), yDataLine+verticalOffset);      	
      }

		}
		
	  /**
	   * Gets the hit shapes associated with the sliders.
	   *
	   * @return an array of hit shapes
	   */
	  public Shape[] getHitShapes() {
	  	return null;
	  }

	  /**
	   * Gets the sliders mark.
	   *
	   * @param points a Point array
	   * @return the mark
	   */
	  public Mark getMark(Point[] points) {
	  	return new Mark() {

				@Override
				public void draw(Graphics2D g, boolean highlighted) {
				}
	  		
	  	};
	  }

		@Override
		public double getXMin() {
			return 0;
		}

		@Override
		public double getXMax() {
			return 100;
		}

		@Override
		public double getYMin() {
			return 0;
		}

		@Override
		public double getYMax() {
			return 100;
		}

		@Override
		public boolean isMeasured() {
			return true;
		}

		@Override
		public Interactive findInteractive(DrawingPanel panel, int _xpix, int _ypix) {
			return null;
		}

		@Override
		public void setEnabled(boolean enabled) {}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void setXY(double x, double y) {}

		@Override
		public void setX(double x) {
		}

		@Override
		public void setY(double y) {}

		@Override
		public double getX() {
			return 0;
		}

		@Override
		public double getY() {
			return 0;
		}

  }
  
  class MySpinner extends JSpinner {
  	
  	public MySpinner (SpinnerModel model) {
  		super(model);
  	}
  	@Override
	public Dimension getPreferredSize() {
  		Dimension dim = super.getPreferredSize();
  		if (dataTrack instanceof ParticleDataTrack) {
  			ParticleDataTrack pdt = (ParticleDataTrack)dataTrack;
  			dim.height = pdt.tp.getModelBuilder().getSpinnerHeight();
  		}
  		else {
    		dim.height += 6;  			
  		}
  		dim.width += 6;
  		return dim;
  	}
  }
  
  class GraphicPanel extends DrawingPanel {
  	GraphicPanel() {
      // remove the interactive panel mouse controller
      removeMouseListener(mouseController);
      removeMouseMotionListener(mouseController);
  	}
  	
		@Override
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
      double mag = FontSizer.getFactor();
			dim.height = (int)(mag * graphicHeight);
			return dim;
		}

  }

}
