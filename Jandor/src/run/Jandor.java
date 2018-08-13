package run;

import session.Session;
import ui.pwidget.JUtil;
import ui.view.CollectionEditorView;
import util.CardUtil;
import util.DebugUtil;
import util.DraftUtil;
import util.DriveUtil;
import util.FileUtil;
import util.ImageUtil;
import util.LoginUtil;
import util.MailUtil;
import util.JedisUtil;
import util.UIManagerUtil;
import util.VersionUtil;
import util.WebUtil;
import accordion.PAccordion;

public class Jandor {
	
	public static void init() {
		init(null);
	}

	public static void init(String jandorHomePath) {
		FileUtil.init(jandorHomePath);
		WebUtil.init();
		VersionUtil.init();
		DebugUtil.init();
		UIManagerUtil.init();
		CardUtil.init();
		Session.init();
		MailUtil.init();
		DraftUtil.init();
		ImageUtil.init();
		JedisUtil.init();
		LoginUtil.init();
		DriveUtil.init();
	}
	
	private Jandor() {}
	
	public static void main(String[] args) {
		init(args.length > 0 ? args[0] : null);
		
		PAccordion accordion = new PAccordion();
		CollectionEditorView.addCollectionEditorView(accordion);
		accordion.build();
		
		JUtil.popupWindow(CollectionEditorView.DEFAULT_TITLE, accordion);
	}
	
}
