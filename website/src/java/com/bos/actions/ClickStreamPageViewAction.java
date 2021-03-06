/* Generated by Together */

package com.bos.actions;
//  java
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.bcop.arch.logger.Logger;
import com.bos.helper.ClickStreamPageViewHelper;

/**
 * This action class is used for the Main screen.<Br>
 */

public class ClickStreamPageViewAction extends BaseAction {

	static {
		logger =
			(Logger) Logger.getLogger(ClickStreamPageViewAction.class.getName());
	}
	
	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has already been completed.
	 * @param mapping    - The ActionMapping used to select this instance
	 * @param actionForm - The optional ActionForm bean for this request (if any)
	 * @param request    - The HTTP request we are processing
	 * @param response   - The HTTP response we are creating
	 * @exception Exception if business logic throws an exception
	 * @return - returns the ActionForward.
	 */
	public ActionForward processAction(
	ActionMapping mapping,
	ActionForm actionForm,
	HttpServletRequest request,
	HttpServletResponse response) {
        System.out.println("!!! Action Hit !!!");
		ActionForward actionForward=null;
		String forwardString = "error";
		String requestId = null;
        actionForward = mapping.findForward("success");
		//run XMLBuilder
        String selectedDate = request.getParameter("selectedDate");
        String sessionTXT = request.getParameter("SessionTxt");
		ClickStreamPageViewHelper cspvh = null;
        
		String dateAsString = null;
		if(selectedDate != null && !selectedDate.equals("")) {
        	StringBuffer b = new StringBuffer(selectedDate);
        	StringBuffer s = b.insert(4,'-');
        	StringBuffer b2 = new StringBuffer(s.toString());
        	StringBuffer s2 = b2.insert(7,'-');
        	dateAsString = s2.toString();
        } else {
        	Calendar calendar = Calendar.getInstance();
         	StringBuffer todaysDate = new StringBuffer(); 
         	todaysDate.append(calendar.get(Calendar.DAY_OF_MONTH));
         	todaysDate.append(calendar.get(Calendar.MONTH));
         	todaysDate.append(calendar.get(Calendar.YEAR));
         	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         	Date d = new Date();
         	dateAsString = sdf.format(d);
         	
        }
    	cspvh = new ClickStreamPageViewHelper(dateAsString,sessionTXT);

        request.setAttribute(DOM, cspvh.getXMLDocument());

		return actionForward;
	}
}

