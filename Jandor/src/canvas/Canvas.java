package canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import util.MouseDetective;
import canvas.handler.MouseHandler;
import canvas.zoom.ZoomAndPanListener;

public class Canvas extends JPanel implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ICanvasLayer> layers;
	private ZoomAndPanListener zoomAndPanListener;
	
	public Canvas(ICanvasLayer... layers) {
		super();
		setBackground(Color.WHITE);
		this.layers = new ArrayList<ICanvasLayer>();
		for(ICanvasLayer layer : layers) {
			addLayer(layer);
		}
		
		zoomAndPanListener = new ZoomAndPanListener(this) {
			
			@Override
			public void handleInit() {
				int centerX = getWidth()/2;
				int centerY = getHeight()/2;
				zoomAndPanListener.virtualZoomOut(centerX, centerY);
				zoomAndPanListener.virtualZoomOut(centerX, centerY);
				zoomAndPanListener.virtualZoomOut(centerX, centerY);
			}
			
		};
		
		//zoomAndPanListener.setMoveCameraEnabled(false);
	}
	
	public ZoomAndPanListener getZoom() {
		return zoomAndPanListener;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
	    RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2.setRenderingHints(rh);
	    rh = new RenderingHints(
	             RenderingHints.KEY_ANTIALIASING,
	             RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    g2.setRenderingHints(rh);
		
		int width = getWidth();
		int height = getHeight();
		
		zoomAndPanListener.transform(g2);
		
		for(ICanvasLayer layer : layers) {
			layer.paintComponent(g2, width, height);
		}
		
		zoomAndPanListener.revert(g2);
	}
	
	public void addLayer(ICanvasLayer layer) {
		if(!layers.contains(layer)) {
			layers.add(layer);
			
			for(Object listener : layer.getListeners()) {
			
				if(listener instanceof MouseListener) {
					addMouseListener((MouseListener) listener);
				}
				
				if(listener instanceof MouseMotionListener) {
					addMouseMotionListener((MouseMotionListener) listener);
				}
				
				if(listener instanceof KeyListener) {
					addKeyListener((KeyListener) listener);
				}
				
			}
		}
	}
	
	public List<ICanvasLayer> getLayers() {
		return layers;
	}

	public void removeLayer(ICanvasLayer layer) {
		if(layers.contains(layer)) {
			layers.remove(layer);
		}
	}

}
