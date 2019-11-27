package github.forvisual.aac.aac_admin_app.ui.page.component;

import github.forvisual.aac.aac_admin_app.WorkImage;

public interface FormResultListener {
	void submitted(WorkImage img, boolean result);
	void moveToReuse(WorkImage image) ;
	void moveToTrashCan(WorkImage image);
}