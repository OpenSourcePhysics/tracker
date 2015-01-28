package org.opensourcephysics.cabrillo.tracker;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.VideoClip;

public class DataModelClipControl extends JPanel implements PropertyChangeListener {
	
	static Color videoColor=Color.WHITE;
	static Color dataColor=videoColor;
	static Color dataClipColor=new Color(51, 200, 51);
	static Color unavaiableDataColor=new Color(51, 200, 51, 127);
	static Color availableDataColor=unavaiableDataColor;
	static int graphicHeight = 100;
	
	protected DataModel model;
	protected DrawingPanel drawingPanel;
	protected JPanel spinnerPanel;
	protected Interactive mappingGraphic;
	protected JLabel videoInLabel, dataInLabel, dataClipLengthLabel, dataStrideLabel;
	protected JSpinner videoInSpinner, dataInSpinner, dataClipLengthSpinner, dataStrideSpinner;
	protected boolean refreshing, drawVideoClip=false;

	public DataModelClipControl(DataModel model) {
		super(new BorderLayout());
		this.model = model;
		model.addPropertyChangeListener("dataclip", this); //$NON-NLS-1$
		model.addPropertyChangeListener("videoclip", this); //$NON-NLS-1$
		model.addPropertyChangeListener("startframe", this); //$NON-NLS-1$
		createGUI();
		refreshSpinners();
		refreshGUI();
	}
	
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
    SpinnerModel spinModel = new SpinnerNumberModel(0, 0, 20, 1);
    videoInSpinner = new MySpinner(spinModel);
    ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (refreshing) return;
        int in = (Integer)videoInSpinner.getValue();
        if (in==model.getStartFrame()) {
        	return;
        }
        model.setStartFrame(in);
        videoInSpinner.setValue(model.getStartFrame());
        repaint();
        model.lastValidFrame = -1;
        model.trackerPanel.repaint();
      }
  	};
  	videoInSpinner.addChangeListener(listener);
  	
    spinModel = new SpinnerNumberModel(0, 0, 20, 1);
    dataInSpinner = new MySpinner(spinModel);
    listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (refreshing) return;
        int in = (Integer)dataInSpinner.getValue();
        if (in==model.getDataClip().getStartIndex()) {
        	return;
        }
        model.getDataClip().setStartIndex(in);
        dataInSpinner.setValue(model.getDataClip().getStartIndex());
        repaint();
        model.lastValidFrame = -1;
        model.trackerPanel.repaint();
      }
  	};
  	dataInSpinner.addChangeListener(listener);

  	
    spinModel = new SpinnerNumberModel(1, 1, 20, 1);
    dataClipLengthSpinner = new MySpinner(spinModel);
    listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (refreshing) return;
        int length = (Integer)dataClipLengthSpinner.getValue();
        if (length==model.getDataClip().getClipLength()) {
        	return;
        }
        model.getDataClip().setClipLength(length);
        dataClipLengthSpinner.setValue(model.getDataClip().getClipLength());
        repaint();
        model.lastValidFrame = -1;
        model.trackerPanel.repaint();
      }
  	};
  	dataClipLengthSpinner.addChangeListener(listener);

    spinModel = new SpinnerNumberModel(1, 1, 10, 1);
    dataStrideSpinner = new MySpinner(spinModel);
    listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (refreshing) return;
        int n = (Integer)dataStrideSpinner.getValue();
        if (n==model.getDataClip().getStride()) {
        	return;
        }
        model.getDataClip().setStride(n);
        dataStrideSpinner.setValue(model.getDataClip().getStride());
        repaint();
        model.lastValidFrame = -1;
        model.trackerPanel.repaint();
      }
  	};
  	dataStrideSpinner.addChangeListener(listener);

  	// assemble
  	drawingPanel = new DrawingPanel() {
  		@Override
  		public Dimension getPreferredSize() {
  			Dimension dim = super.getPreferredSize();
  			dim.height = graphicHeight;
  			return dim;
  		}
  	};
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
	
	protected void refreshSpinners() {
		DataClip clip = model.getDataClip();
    VideoClip videoClip = model.getVideoClip();
    
		int clipLength = clip.getDataLength();
    SpinnerModel spinModel = new SpinnerNumberModel(clip.getStartIndex(), 0, clipLength-1, 1);
    dataInSpinner.setModel(spinModel);
    spinModel = new SpinnerNumberModel(clip.getStride(), 1, clipLength-1, 1);
    dataStrideSpinner.setModel(spinModel);
    int length = clipLength;
    if (videoClip!=null) {
  		int first = videoClip.getFirstFrameNumber();
  		int last = videoClip.getLastFrameNumber();
      spinModel = new SpinnerNumberModel(model.getStartFrame(), first, last, 1);
      videoInSpinner.setModel(spinModel);
      int videoLength = last-first+1;
      length = Math.min(clipLength, videoLength);
    }
    spinModel = new SpinnerNumberModel(clip.getClipLength(), 1, length, 1);
    dataClipLengthSpinner.setModel(spinModel);	
    if (model.trackerPanel!=null) {
    	model.trackerPanel.getModelBuilder().refreshSpinners();
    }
	}

	protected void refreshGUI() {
		setBorder(BorderFactory.createTitledBorder("Data Clip"));
  	videoInLabel.setText("Frame Start");
  	dataClipLengthLabel.setText("Frame Count");
  	dataInLabel.setText("Data Start");
  	dataStrideLabel.setText("Data Stride");
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
//		if (e.getPropertyName().equals("dataclip")) { //$NON-NLS-1$
//			refreshSpinners();
//		}
//		else if (e.getPropertyName().equals("videoclip")) { //$NON-NLS-1$
//			refreshSpinners();
//		}
	}
  
  /**
   * Gets the last displayed clip index. An index is displayed if its
   * corresponding frame is less than or equal to the video's last frame number. 
   *
   * @return the index
   */
  private int getLastDisplayedClipIndex() {
  	int stepCount = model.getDataClip().getClipLength();
    VideoClip clip = model.trackerPanel.getPlayer().getVideoClip();
    int last = clip.getLastFrameNumber();
    for (int i = stepCount-1; i>0; i--) {
    	// determine corresponding frame number and index
    	int frame = model.getStartFrame()+i;
    	int index = model.getDataClip().stepToIndex(i);
    	// look for first step with frame<=last frame and index<data length
      if (frame<=last && index<model.getDataClip().getDataLength()) {
      	// return the index
        return index;
      }
    }
    return model.getDataClip().stepToIndex(0);
  }
  
  public void setGraphic(Interactive graphic) {
  	if (graphic==null) return;
  	if (mappingGraphic!=null) {
    	drawingPanel.removeDrawable(mappingGraphic);
  	}
  	mappingGraphic = graphic;
  	drawingPanel.addDrawable(mappingGraphic);
  }
  
  class MappingGraphic implements Interactive {
  	
		GeneralPath path = new GeneralPath();
		//pig convert to using TPoints

		@Override
		public void draw(DrawingPanel panel, Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
            
      int strokeWidth = 8;
      g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
      
      // fill background
      Rectangle rect = new Rectangle(drawingPanel.getSize());
      g2.setColor(new Color(200, 200, 200));
      g2.fill(rect);
			
			int yVideoLine = 30;
      int yDataLine = 70;
      
      // draw video and data labels
      g2.setColor(Color.DARK_GRAY);
      g2.setFont(videoInLabel.getFont());
      RenderingHints rh = g2.getRenderingHints();
      rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
			int largeGap = 10, smallGap = 4;
      int fontDrop = g2.getFontMetrics().getHeight()/4;
      String s = "Video";
      int labelSpace = g2.getFontMetrics().stringWidth(s);
      g2.drawString(s, largeGap, yVideoLine+fontDrop);

      s = "Data";
      labelSpace = Math.max(labelSpace, g2.getFontMetrics().stringWidth(s));
      g2.drawString(s, largeGap, yDataLine+fontDrop);
      
      labelSpace += 6;
      
      VideoClip videoClip = model.getVideoClip();
      DataClip dataClip = model.getDataClip();
      
      s = "0"; //$NON-NLS-1$
      int frontSpace = g2.getFontMetrics().stringWidth(s);
      frontSpace = Math.max(frontSpace, g2.getFontMetrics().stringWidth(s));

      s = String.valueOf(videoClip.getLastFrameNumber());
      int endSpace = g2.getFontMetrics().stringWidth(s);
      s = String.valueOf(dataClip.getDataLength()-1);
      endSpace = Math.max(endSpace, g2.getFontMetrics().stringWidth(s));

      int videoClipFrames = videoClip.getLastFrameNumber()-videoClip.getFirstFrameNumber()+1;
      if (drawVideoClip) {
      	videoClipFrames = videoClip.getFrameCount();
      }
      int dataClipFrames = dataClip.getDataLength();
			int maxFrames = Math.max(videoClipFrames, dataClipFrames);
			
			double maxLength = panel.getWidth()-labelSpace-frontSpace-endSpace-3*largeGap-2*smallGap;
      double lengthPerFrame = maxLength/maxFrames;
      
			// draw video line		
			double lineLength = maxLength*videoClipFrames/maxFrames;
			double leftEnd= labelSpace+2*largeGap+frontSpace;
			double rightVideoEnd = leftEnd+lineLength;
      path.reset();
      path.moveTo(leftEnd, yVideoLine);
      path.lineTo(rightVideoEnd, yVideoLine);
      g2.setColor(videoColor);
      g2.draw(path);
      
			// draw dataclip on video line--full clip first as "unavailable"
      double fullClipLength = lengthPerFrame*(dataClip.getClipLength());
      int frame0 = drawVideoClip? videoClip.getStartFrameNumber(): 0;
      double videoStartClip = leftEnd+lengthPerFrame*(model.getStartFrame()-frame0);
      double videoEndClip = videoStartClip + fullClipLength;
      videoStartClip = Math.max(videoStartClip, leftEnd);
      videoEndClip = Math.min(videoEndClip, leftEnd+lineLength);
      path.reset();
      path.moveTo(videoStartClip, yVideoLine);
      path.lineTo(videoEndClip, yVideoLine);
      g2.setColor(unavaiableDataColor);
      g2.draw(path);
      
			// draw dataclip on video line--available clip
      videoStartClip = leftEnd+lengthPerFrame*(model.getStartFrame()-frame0);
      double clipLength = lengthPerFrame*(dataClip.getAvailableClipLength());
      videoEndClip = videoStartClip + clipLength;
      videoStartClip = Math.max(videoStartClip, leftEnd);
      videoEndClip = Math.min(videoEndClip, leftEnd+lineLength);
      // adjust clip length in case video truncated it
      clipLength = videoEndClip - videoStartClip;
      path.reset();
      path.moveTo(videoStartClip, yVideoLine);
      path.lineTo(videoEndClip, yVideoLine);
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
      path.lineTo(videoStartClip, yVideoLine+strokeWidth/2);
      g2.draw(path);
      path.reset();
      path.moveTo(dataEndClip, yDataLine-strokeWidth/2);
      path.lineTo(videoEndClip, yVideoLine+strokeWidth/2);
      g2.draw(path);
      
      // draw frame numbers
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN));
      int verticalOffset = fontDrop;
      s = "0"; //$NON-NLS-1$
      g2.drawString(s, (int)(leftEnd-frontSpace-smallGap), yDataLine+verticalOffset);
      s = String.valueOf(dataClipFrames-1);
      g2.drawString(s, (int)(rightDataEnd+smallGap), yDataLine+verticalOffset);
      s = String.valueOf(videoClip.getFirstFrameNumber());
      g2.drawString(s, (int)(leftEnd-frontSpace-smallGap), yVideoLine+verticalOffset);
      s = String.valueOf(videoClip.getLastFrameNumber());
      g2.drawString(s, (int)(rightVideoEnd+smallGap), yVideoLine+verticalOffset);
      
      verticalOffset = -2-strokeWidth/2;
      s = String.valueOf(model.getStartFrame());
      g2.drawString(s, (int)(videoStartClip), yVideoLine+verticalOffset);
    	int n = model.getStartFrame()+dataClip.getAvailableClipLength()-1;
    	n = Math.min(n, videoClip.getLastFrameNumber());
      if (model.getStartFrame()!=n) {
	      s = String.valueOf(n);
	      int space = g2.getFontMetrics().stringWidth(s);
	      g2.drawString(s, (int)(videoEndClip-space), yVideoLine+verticalOffset);      	
      }
      
      verticalOffset = fontDrop+strokeWidth+3;
      s = String.valueOf(model.getDataClip().getStartIndex());
      g2.drawString(s, (int)(dataStartClip), yDataLine+verticalOffset);
    	n = getLastDisplayedClipIndex();
      if (n-model.getDataClip().getStartIndex()>0) {
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

				@Override
				public Rectangle getBounds(boolean highlighted) {
					return null;
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
			// pig implement
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
			// pig implement
		}

		@Override
		public void setY(double y) {}

		@Override
		public double getX() {
			// pig implement
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
  	public Dimension getPreferredSize() {
  		Dimension dim = super.getPreferredSize();
  		dim.height += 6;
  		dim.width += 6;
  		return dim;
  	}
  }

}
