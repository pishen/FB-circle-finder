package team13.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class FBUserCell extends AbstractCell<FBUser>{

	@Override
	public void render(Context context, FBUser fbUser, SafeHtmlBuilder sb) {
		if(fbUser == null){
			return;
		}
		
		sb.appendHtmlConstant("<table>");
		sb.appendHtmlConstant("<tr>");
		sb.appendHtmlConstant("<td>");
		sb.appendHtmlConstant("<img src='" + fbUser.getAvatarURL() + "'/>");
		sb.appendHtmlConstant("</td>");
		sb.appendHtmlConstant("<td>");
		sb.appendEscaped(fbUser.getUserName());
		sb.appendHtmlConstant("</td>");
		sb.appendHtmlConstant("</tr>");
		sb.appendHtmlConstant("</table>");
		
	}

}
