package vdb.mydb.jsp.action.catalog;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import vdb.metacat.fs.page.PagesManager;
import vdb.metacat.fs.page.UpdateItemPage;
import vdb.mydb.VdbManager;
import vdb.mydb.vtl.action.ServletActionProxy;

public class UpdateUpdateItems extends ServletActionProxy
{
	public void doAction(ServletRequest request, ServletResponse response,
			ServletContext servletContext) throws Exception
	{

		MakeReq2Page mrp = new MakeReq2Page();
		UpdateItemPage uip = mrp.makeUpdateItemPage(request);

		PagesManager pm = (PagesManager) VdbManager.getEngine()
				.getApplicationContext().getBean("pagesManager");
		pm.updatePage(uip);

	}

}
