package util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import run.Jandor;
import ui.GuiCard;
import ui.pwidget.ColorUtil;
import canvas.CardLayer;
import canvas.IRenderable;
import canvas.animation.Animator;
import deck.Card;
import dice.CounterRenderer;
import dice.Die;
import dice.DieRenderer;
import dice.TokenRenderer;

public class ImageUtil {

	public static final double DEFAULT_SCALE = 1.0; //0.8; //0.675;

	private static Map<File, BufferedImage> imagesToCacheToDisc = new HashMap<File, BufferedImage>();
	private static final Map<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();
	private static double scale = DEFAULT_SCALE;
	private static boolean WARNED = false;
	private static final int DEFAULT_CORNER_RADIUS = 22;
	public static final String ICON_D10 = "d10-green.png";
	private static boolean IGNORE_NULL = false;

	private static final int DEFAULT_CARD_WIDTH = 223;
	private static final int DEFAULT_CARD_HEIGHT = 311;

	private static final Pattern patternMultiverse = Pattern.compile("(.*multiverseid=)(.*?)(&.*)");

	private static Map<String, List<JLabel>> imageCacheListeners = new HashMap<String, List<JLabel>>();

	private ImageUtil() {}

	public static void init() {
		startImageDiscCachingListener();
	}

	public static void setIgnoreNull(boolean ignoreNull) {
		IGNORE_NULL = ignoreNull;
	}

	public static int getCardCornerRadius(double scale) {
		return (int) Math.round(getScale() * DEFAULT_CORNER_RADIUS * scale);
	}

	public static void quickDisplayImage(BufferedImage img, String title) {
		JFrame f = new JFrame(title);
		f.getContentPane().add(new JLabel(new ImageIcon(img)));
		f.pack();
		Random r = new Random();
		f.setLocation(r.nextInt(1720), r.nextInt(1080));
		f.setVisible(true);
	}

	public static String getUrl(int multiverseId) {
		/*File cachedImageFile = FileUtil.getCachedImageFile(multiverseId);
    	if(multiverseId != -1 && cachedImageFile.exists()) {
    		return cachedImageFile.toURI().toString();
    	}*/
		return "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + multiverseId + "&type=card";
	}

	public static String getResourceUrl(String fileName) {
		URL resource = Jandor.class.getResource("/images/" + fileName);
		if(resource == null) {
			return getResourceUrl("close_button.png");
		}
		return resource.toString();
	}

	public static BufferedImage readResourceImage(String fileName) {
		return ImageUtil.readImage(getResourceUrl(fileName), fileName);
	}

	public static String getSymbolUrl(String symbolName, String size) {
		if(size.equals(ManaUtil.SIZE_SMALL)) {
			return getResourceUrl(symbolName + ".jpg");
		}
		return "http://gatherer.wizards.com/Handlers/Image.ashx?size=" + size + "&name=" + symbolName + "&type=symbol";
	}

	public static BufferedImage readImage(int multiverseId, String name) {
		return readImage(multiverseId, getScale(), name);
	}

	public static BufferedImage readImage(int multiverseId, double scale, String name) {
		return readImage(getUrl(multiverseId), scale, name);
	}

	public static BufferedImage readImage(String urlString, String name) {
		return readImage(urlString, getScale(), name);
	}

	public static BufferedImage readImage(String urlString, double scale, String name) {
		if(urlString == null) {
			return null;
		}

		if(urlString.contains("-1")) {
			urlString = ImageUtil.getResourceUrl("back-custom.png");
		}
		String key = urlString + ":" + (int) (scale * 100);
		if(imageCache.containsKey(key)) {
			return imageCache.get(key);
		}

		BufferedImage image = null;
		String fullKey = null;

		try {
			fullKey = urlString + ":" + 100;
			if(imageCache.containsKey(fullKey)) {
				image = imageCache.get(fullKey);
			} else {

				URL url = null;
				url = new URL(urlString);
				image = CustomImageIORead(url, name);

			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			// Internet is likely out in this case, let the next block handle it
		}

		try {
			boolean forceRounded = false;
			if(image == null) {
				if(!WARNED && !IGNORE_NULL && !DebugUtil.OFFLINE_MODE) {
					//JOptionPane.showMessageDialog(null, "Gatherer is likely down or your internet connection is not working. All images will be \"Jandor's Saddlebags.\" You're welcome.", "Could Not Load Images", JOptionPane.WARNING_MESSAGE);
					WARNED = true;
				}
				if(urlString.contains("-1")) {
					image = ImageIO.read(Jandor.class.getResource("/images/back-custom.png"));
				} else {

					if(CardUtil.isBasicLandName(name)) {
						image = ImageIO.read(Jandor.class.getResource("/images/" + name.toLowerCase() + ".jpg"));
						forceRounded = true;
					} else {

						//int idx = ShuffleUtil.randInt(2);
						//image = ImageIO.read(Jandor.class.getResource("/images/jandor-" + idx + ".jpg"));
						Card card = new Card(name);
						Color manaColor = ManaUtil.getColor(card);
						String colorName = ManaUtil.getColorName(manaColor);
						image = ImageIO.read(Jandor.class.getResource("/images/card-" + colorName + ".png"));
						if(name != null) {
							//image = label(image, name, 25, 25);
							image = renderCardFromScratch(image, name, manaColor);
							forceRounded = true;
						}

					}
				}
			}

			if(forceRounded || urlString.contains("type=card") || urlString.contains("back.png") || urlString.contains("back-custom.png")) {
				image = makeRoundedCorners(image, getCardCornerRadius(1.0));
			}
			if(scale != 1.0) {
				if(!imageCache.containsKey(fullKey)) {
					imageCache.put(fullKey, image);
					//System.out.println("Caching image: " + fullKey);
				}
				image = scale(image, scale);
			}
			imageCache.put(key, image);
			//System.out.println("Caching image: " + key);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		return image;
	}

	public static boolean isCached(String urlString, double scale) {
		String key = urlString + ":" + (int) (scale * 100);
		return imageCache.containsKey(key);
	}

	public static BufferedImage scale(BufferedImage img, int width, int height) {
		Image scaled = img.getScaledInstance(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bimage = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(scaled, 0, 0, null);
	    bGr.dispose();
	    return bimage;
	}

	public static Image scale(Image img, int width) {
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		float ratio = h / (float) w;
		return img.getScaledInstance(width, (int) (width*ratio), BufferedImage.TYPE_INT_ARGB);
	}

	public static Image scaleHeight(Image img, int height) {
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		float ratio = w / (float) h;
		if(h < height) {
			return img;
		}
		return img.getScaledInstance((int) (height*ratio), height, BufferedImage.TYPE_INT_ARGB);
	}

	public static BufferedImage scale(BufferedImage img, double scale) {
		if(scale == 1.0) {
			return img;
		}
		return scale(img, (int) (img.getWidth() * scale));
	}

	public static BufferedImage scale(BufferedImage img, int width) {
		int w = img.getWidth();
		int h = img.getHeight();
		float ratio = h / (float) w;
		return toBufferedImage(img.getScaledInstance(width, (int) (width*ratio), BufferedImage.TYPE_INT_ARGB));
	}

	public static BufferedImage toBufferedImage(Image img) {
		BufferedImage newImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return newImage;
	}

	public static BufferedImage fixSize(BufferedImage img, int maxDimension) {
		int w = img.getWidth();
		int h = img.getHeight();

		if(w < maxDimension) {
			return img;
		}

		int maxDim = w;
		float s = maxDimension / (float) maxDim;

		int newW = (int)(w*s);
		int newH = (int)(h*s);

		Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage newImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(scaled, 0, 0, null);
		g.dispose();
		return newImage;
	}

	/**
	 * Rotates an image. Actually rotates a new copy of the image.
	 *
	 * @param img The image to be rotated
	 * @param angle The angle in degrees
	 * @return The rotated image
	 */
	public static BufferedImage rotate(BufferedImage img, double angle)
	{
	    double sin = Math.abs(Math.sin(Math.toRadians(angle))),
	           cos = Math.abs(Math.cos(Math.toRadians(angle)));

	    int w = img.getWidth(null), h = img.getHeight(null);

	    int neww = (int) Math.floor(w*cos + h*sin),
	        newh = (int) Math.floor(h*cos + w*sin);

		BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();

	    g.translate((neww-w)/2, (newh-h)/2);
	    g.rotate(Math.toRadians(angle), w/2, h/2);
	    g.drawRenderedImage(img, null);
	    g.dispose();

	    return bimg;
	}

	public static BufferedImage label(BufferedImage img, String text, int x, int y) {
		int w = img.getWidth();
		int h = img.getHeight();

		BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();
	    g.drawRenderedImage(img, null);
	    g.setColor(Color.BLACK);
	    g.fillRect(10, 10, w - 2*x, y);
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Purisa", Font.BOLD, 16));
	    g.setStroke(new BasicStroke(CardLayer.STROKE_SELECT));
	    g.drawString(text, x, y);
	    g.dispose();

	    return bimg;
	}

	public static BufferedImage renderCardFromScratch(BufferedImage img, String name, Color manaColor) {
		int w = img.getWidth();
		int h = img.getHeight();
		GuiCard guiCard = new GuiCard(name, w, h);

		BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();
	    g.drawRenderedImage(img, null);
	    g.setColor(ColorUtil.getBestForegroundColor(manaColor));
	    g.setFont(new Font("Purisa", Font.BOLD, 16));
	    g.setStroke(new BasicStroke(CardLayer.STROKE_SELECT));
	    guiCard.printLabels(g, manaColor, w, h);
	    g.dispose();

	    return bimg;
	}

	public static double getScale() {
		return scale;
	}

	public static void setScale(double s) {
		scale = s;
		imageCache.clear();
	}

	public static void restoreScale() {
		scale = DEFAULT_SCALE;
	}

	public static boolean isScaleRestored() {
		return scale == DEFAULT_SCALE;
	}

	public static BufferedImage makeRoundedCorners(BufferedImage image, int cornerRadius) {
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();

	    // This is what we want, but it only does hard-clipping, i.e. aliasing
	    // g2.setClip(new RoundRectangle2D ...)

	    // so instead fake soft-clipping by first drawing the desired clip shape
	    // in fully opaque white with antialiasing enabled...
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

	    // ... then compositing the image on top,
	    // using the white shape from above as alpha source
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(image, 0, 0, null);

	    g2.dispose();

	    return output;
	}

	public static ImageIcon getImageIcon(String resourceName, int height) {
		return new ImageIcon(ImageUtil.scaleHeight(ImageUtil.readResourceImage(resourceName), height));
	}

	public static ImageIcon getImageIcon(String resourceName, int width, int height) {
		return new ImageIcon(ImageUtil.scale(ImageUtil.readResourceImage(resourceName), width, height));
	}

	public static ImageIcon getImageIcon(String resourceName) {
		return new ImageIcon(ImageUtil.readResourceImage(resourceName));
	}

	public static BufferedImage getJandorIcon() {
    	return ImageUtil.readResourceImage("jandor-icon.png");
    }

    public static BufferedImage getCloseIcon() {
    	return ImageUtil.readResourceImage("close_button.png");
    }

    public static BufferedImage getCloseIconFull() {
     	return ImageUtil.readResourceImage("close_button_full.png");
    }

    public static BufferedImage getCloseIconFullDown() {
     	return ImageUtil.readResourceImage("close_button_full_down.png");
    }

    public static BufferedImage getEditIcon() {
    	return ImageUtil.readResourceImage("pencil_button.png");
    }

    public static BufferedImage getEditIconFull() {
     	return ImageUtil.readResourceImage("pencil_button_full.png");
    }

    public static BufferedImage getEditIconFullDown() {
     	return ImageUtil.readResourceImage("pencil_button_full_down.png");
    }

    public static String getDieIconUrl(Die die) {
    	if(die.getRenderer() instanceof DieRenderer && die.getMinValue() == 0 && die.getMaxValue() == 9) {
    		Color c = die.getColor();
    		if(c.equals(Color.RED)) {
    			return ImageUtil.getResourceUrl("d10-red.png");
    		} else if(c.equals(Color.GREEN)) {
    			return ImageUtil.getResourceUrl("d10-green.png");
    		} else if(c.equals(Color.BLUE)) {
    			return ImageUtil.getResourceUrl("d10-blue.png");
    		} else if(c.equals(Color.BLACK)) {
    			return ImageUtil.getResourceUrl("d10-black.png");
    		} else if(c.equals(Color.GRAY)) {
    			return ImageUtil.getResourceUrl("d10-gray.png");
    		} else if(c.equals(Color.YELLOW)) {
    			return ImageUtil.getResourceUrl("d10-gold.png");
    		}
    		return ImageUtil.getResourceUrl("d10-white.png");
    	}
    	if(die.getRenderer() instanceof CounterRenderer) {
    		Color c = die.getColor();
    		if(c.equals(Color.RED)) {
    			return ImageUtil.getResourceUrl("gem-red.png");
    		} else if(c.equals(Color.GREEN)) {
    			return ImageUtil.getResourceUrl("gem-green.png");
    		} else if(c.equals(Color.BLUE)) {
    			return ImageUtil.getResourceUrl("gem-blue.png");
    		} else if(c.equals(Color.BLACK)) {
    			return ImageUtil.getResourceUrl("gem-black.png");
    		} else if(c.equals(Color.GRAY)) {
    			return ImageUtil.getResourceUrl("gem-gray.png");
    		} else if(c.equals(Color.YELLOW)) {
    			return ImageUtil.getResourceUrl("gem-gold.png");
    		}
    		return ImageUtil.getResourceUrl("gem-white.png");
    	}
    	if(die.getRenderer() instanceof TokenRenderer) {
    		Color c = die.getColor();
    		if(c.equals(Color.RED)) {
    			return ImageUtil.getResourceUrl("token-red.png");
    		} else if(c.equals(Color.GREEN)) {
    			return ImageUtil.getResourceUrl("token-green.png");
    		} else if(c.equals(Color.BLUE)) {
    			return ImageUtil.getResourceUrl("token-blue.png");
    		} else if(c.equals(Color.BLACK)) {
    			return ImageUtil.getResourceUrl("token-black.png");
    		} else if(c.equals(Color.GRAY)) {
    			return ImageUtil.getResourceUrl("token-gray.png");
    		} else if(c.equals(Color.YELLOW)) {
    			return ImageUtil.getResourceUrl("token-gold.png");
    		}
    		return ImageUtil.getResourceUrl("token-white.png");
    	}
    	return ImageUtil.getResourceUrl(ICON_D10);
    }

    public static void save(BufferedImage image, String name) {
    	File outputFile = new File("resources/images/" + name + ".jpg");
		try {
			ImageIO.write(image, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static <T extends IRenderable> void cacheImageInBackground(List<T> renderables, final double scale) {
    	cacheImageInBackground(renderables, scale, null);
    }

    public static <T extends IRenderable> void cacheImageInBackground(List<T> renderables, final double scale, final Component view) {
    	Animator<T> a = new Animator<T>(view, renderables) {

			@Override
			public boolean update(T obj, int step) {
				BufferedImage img = obj.getRenderer().getImage(scale);
				String urlString = obj.getRenderer().getImageUrl();
				for(JLabel label : getImageCacheListeners(obj.getRenderer().getImageUrl(), scale)) {
			 		label.setIcon(new ImageIcon(ImageUtil.readImage(urlString, scale, urlString)));
				}
				clearImageCacheListeners(urlString, scale);
				if(view != null) {
					view.repaint();
				}
				return true;
			}

    	};
    	a.start();
    }

    public static void addImageCacheListener(String urlString, JLabel label, double scale) {
		String key = urlString + ":" + (int) (scale * 100);
    	if(!imageCacheListeners.containsKey(key)) {
    		imageCacheListeners.put(key, new ArrayList<JLabel>());
    	}
    	if(!imageCacheListeners.get(key).contains(label)) {
    		imageCacheListeners.get(key).add(label);
    	}
    	if(isCached(urlString, scale)) {
    		label.setIcon(new ImageIcon(ImageUtil.readImage(urlString, scale, urlString)));
    	}
    }

    public static List<JLabel> getImageCacheListeners(String urlString, double scale) {
    	String key = urlString + ":" + (int) (scale * 100);
    	if(!imageCacheListeners.containsKey(key)) {
    		return new ArrayList<JLabel>();
    	}
    	return imageCacheListeners.get(key);
    }

    public static void clearImageCacheListeners(String urlString, double scale) {
    	String key = urlString + ":" + (int) (scale * 100);
    	if(imageCacheListeners.containsKey(key)) {
    		imageCacheListeners.remove(key);
    	}
    }

    public static int getMultiverseId(String urlString) {
    	Matcher m = patternMultiverse.matcher(urlString.toLowerCase());
    	if(m.find()) {
    		return Integer.valueOf(m.group(2));
		}
    	return -1;
    }

    public static boolean isImageAvailable(String urlString, String name) {
    	/*BufferedImage image = null;
    	try {
    		URL url = null;
    		url = new URL(urlString);
    		image = CustomImageIORead(url, name);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			// Internet is likely out in this case, let the next block handle it
		}
    	return image != null;*/

    	File file = FileUtil.getCachedImageFile(getMultiverseId(urlString));
    	return !DebugUtil.OFFLINE_MODE || (file != null && file.exists());
    }

    public static synchronized BufferedImage CustomImageIORead(URL url, String name) throws IOException {
    	int multiverseId = getMultiverseId(url.toString());
    	final File cachedImageFile = FileUtil.getCachedImageFile(multiverseId);
    	if(cachedImageFile != null && cachedImageFile.exists()) {
    		BufferedImage cachedImage = null;
    		try {
    			cachedImage = ImageIO.read(cachedImageFile);
    		} catch (IndexOutOfBoundsException e) {
    			// Do nothing. Known bug.
    			cachedImage = null;
    		}
    		if(cachedImage != null) {

    			// Fix the cache's image size if possible
    			BufferedImage resizedImage = maybeFixCardSize(cachedImage, true);
    			if(resizedImage != null) {
    				addImageToCacheToDisc(cachedImageFile, resizedImage);
    				return resizedImage;
    			}

    			return cachedImage;
    		}
    	}

    	if(DebugUtil.OFFLINE_MODE && url != null && url.toString().contains("http")) {
    		return null;
    	}

    	BufferedImage image = null;

    	try {
    		image = ImageIO.read(url);

    		// If we've read our image from a url, we need to assure that it meets our criteria
    		if(multiverseId > -1) {
    			image = maybeFixCardSize(image);
    		}

    	} catch (IndexOutOfBoundsException e) {
    		// Do nothing. Known bug.
    	}

    	// Cache our image, we can add a check for a user preference flag here.
    	if(DebugUtil.CACHE_IMAGES_FOR_OFFLINE && cachedImageFile != null && !cachedImageFile.exists() && !CardUtil.isBasicLandName(name)) {
    		addImageToCacheToDisc(cachedImageFile, image);
    	}

    	return image;
    }

    public static String getImageHtml(Card card) {
    	String urlString = card.getImageUrl();
    	String transformUrlString = card.canTransform() ? card.getTransformImageUrl() : null;
    	if(!isImageAvailable(urlString, card.getName())) {
    		return card.getToolTipText(200, false, true).replace("<html>", "").replace("</html>", "");
    	}

    	if(transformUrlString == null) {
    		return "<img src=\"" + urlString + "\"/>";
    	}
    	return "<img src=\"" + urlString + "\"/><img src=\"" + transformUrlString + "\"/>";
    }

    private static void addImageToCacheToDisc(final File file, final BufferedImage image) {
    	imagesToCacheToDisc.put(file, image);
    }

    private static void cacheImagesToDisc() {
    	Map<File, BufferedImage> copyCachedImagesToDisc = new HashMap<File, BufferedImage>(imagesToCacheToDisc);
    	if(copyCachedImagesToDisc.size() > 0) {
    		System.out.println("Found " + copyCachedImagesToDisc.size() + " images to cache to disc.");
    	}
    	for(File file : copyCachedImagesToDisc.keySet()) {
    		try {
    			ImageIO.write(copyCachedImagesToDisc.get(file), "png", file);
				imagesToCacheToDisc.remove(file);
    		} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

    private static BufferedImage maybeFixCardSize(BufferedImage image) {
    	return maybeFixCardSize(image, false);
    }

    private static BufferedImage maybeFixCardSize(BufferedImage image, boolean nullIfNoChange) {
    	if(image == null) {
    		return null;
    	}
    	int w = image.getWidth();
    	int h = image.getHeight();
    	int allowance = 2;
    	if(Math.abs(w - DEFAULT_CARD_WIDTH) > allowance || Math.abs(h - DEFAULT_CARD_HEIGHT) > allowance) {
    		return scale(image, DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
    	}
    	return nullIfNoChange ? null : image;
    }

    private static void startImageDiscCachingListener() {
    	new Thread() {

			@Override
			public void run() {
				if(DebugUtil.OFFLINE_MODE || !DebugUtil.CACHE_IMAGES_FOR_OFFLINE) {
					return;
				}

				System.out.println("Listening for images cache to disc");
				while(!DebugUtil.OFFLINE_MODE && DebugUtil.CACHE_IMAGES_FOR_OFFLINE) {
					cacheImagesToDisc();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}.start();
    }

}
