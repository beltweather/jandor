package canvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ui.pwidget.CloseListener;
import util.ImageUtil;
import util.SerializationUtil;
import zone.ZoneType;
import deck.Card;
import deck.RenderableList;

public class LightCardLayer extends CardLayer implements ICanvasLayer, CloseListener, Serializable {

	private static final long serialVersionUID = 1L;

	private RenderableList<IRenderable> renderables;
	
	public LightCardLayer(Canvas canvas) {
		super(canvas, new RenderableList<Card>(), false);
		this.renderables = new RenderableList<IRenderable>();
		backgroundFileName = "background-0-light.png";
	}

	public void setSerializedCardList(String serializedRenderables) {
		renderables = (RenderableList<IRenderable>) SerializationUtil.fromString(serializedRenderables);
	}
	
	public void setSerializedCardList(byte[] serializedRenderables) {
		renderables = (RenderableList<IRenderable>) SerializationUtil.fromBytes(serializedRenderables);
		screenW = renderables.screenW;
		screenH = renderables.screenH;
	}
	
	@Override
	public void handleClosed() {
	
	}

	@Override
	public void paintComponent(Graphics2D g, int width, int height) {
		update(width, height);
		
		//paintBackground(g, screenW, screenH);
		getCanvas().getZoom().revert(g);
		paintZones(g, screenW, screenH);
		getCanvas().getZoom().transform(g);
		
		//getCanvas().getZoom().revert(g);
		//g.transform(AffineTransform.getRotateInstance(Math.PI, width/2, height/2));
		//getCanvas().getZoom().transform(g);
		paintRenderables(g, screenW, screenH);
		//paintBanner(g, width, height);
	}
	
	@Override
	protected void update(int width, int height) {
		int lastScreenW = screenW;
		int lastScreenH = screenH;
		screenW = width;
		screenH = height;
		
		if(screenW == lastScreenW && screenH == lastScreenH) {
			return;
		}
		
		int dx = screenW - lastScreenW;
		int dy = screenH - lastScreenH;
		for(IRenderable r : renderables) {
			if(dx != 0) {
				if(r.getRenderer().getZoneType() == ZoneType.GRAVEYARD) {
					r.getRenderer().setScreenX(Math.max(0, r.getRenderer().getScreenX() + dx));
				} else if(r.getRenderer().getZoneType() == ZoneType.HAND) {
					r.getRenderer().setScreenX(Math.max(0, r.getRenderer().getScreenX() + dx/2));
				}
			}
			if(dy != 0) {
				r.getRenderer().setScreenY(Math.max(0, r.getRenderer().getScreenY() + dy));
			}
		}
		
		updateZoneBounds();
	}

	private void paintRenderables(Graphics2D g, int width, int height) {
		for(IRenderable r : renderables) {
			r.getRenderer().paintComponent(this, g, width, height);
		}
	}
	
	private void paintBanner(Graphics g, int width, int height) {
		String currentUsername = getCurrentUsername();
		
		int bannerHeight = 30;
		g.setClip(0, 0, width, height);
		g.setColor(new Color(255,0,0,100));
		g.fillRect(0, 0, width, bannerHeight);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Helvetica", Font.BOLD, 20));
		String s = currentUsername == null ? "Watching: Opponent" : "Streaming: " + currentUsername;
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, screenW/2 - ((int) bounds.getWidth())/2, 20);
	}
	
	protected BufferedImage getBackground() {
		backgroundFileName = OPPONENT_BACKGROUND_FILENAME;
		if(backgroundFileName == null) {
			return null;
		}
		return ImageUtil.readImage(ImageUtil.getResourceUrl(backgroundFileName), backgroundFileName);
	}
	
	@Override
	public RenderableList<IRenderable> getAllObjects() {
		return renderables;
	}
	
	@Override
	public RenderableList<IRenderable> getExtraRenderables() {
		return renderables;
	}
	
}
