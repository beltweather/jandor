package canvas;

import java.io.Serializable;

public interface IRenderable<T> extends Serializable {

	public IRenderer<T> getRenderer();
	
	public String getToolTipText();
	
	public String getName();
	
	public IRenderable<T> copyRenderable();
	
}
