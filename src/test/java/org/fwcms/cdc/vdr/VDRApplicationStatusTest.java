package org.fwcms.cdc.vdr;

import static com.olo.util.PropertyReader.configProp;

import java.io.FileReader;
import java.util.HashMap;
 
import org.fwcms.initiators.VDRInitiator;
import org.fwcms.pages.CommonElements;
import org.fwcms.pages.cdc.LoginPage;
import org.fwcms.pages.cdc.vdr.ApplicationStatusPage;
import org.fwcms.pages.cdc.vdr.HomePage;
import org.fwcms.pages.cdc.vdr.VdrDetailsPage;
import org.fwcms.pages.cdc.vdr.WorkerSelectionPage;
import org.fwcms.prop.cdc.vdrApplicationPageProp;
import org.fwcms.util.EmployerCredentialsProp;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VDRApplicationStatusTest extends VDRInitiator {
	
	@DataProvider(name="vdrValidationData")
	public Object[][] searchVdrByApplicationStatus() throws Exception{
		JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(VDRApplicationStatusTest.class.getResource("/org/fwcms/cdc/vdr/vdrValidationData.json").getFile()));
		Object[][] result = new Object[ja.size()][1];
		for(int i=0;i<ja.size();i++){
			result[i][0] = ja.get(i);
		}
		return result;
	}
	
	public static final String cdcUrl = configProp.getProperty("cdcUser");
	
	@Test(dataProvider="vdrValidationData")
	public void testSearchVDRByStatusFlow(HashMap<String, String> data) throws Exception {
		WebDriver driver = getDriver();	
		driver.get(cdcUrl);	
		new LoginPage(getDriver()).initElements().loginToCDC(EmployerCredentialsProp.getVdrEmployerUserName().trim(), EmployerCredentialsProp.getVdrEmployerPassword().trim());
		CommonElements cm = new CommonElements(driver).initElements();
		HomePage hp = new HomePage(driver).initElements();
		hp.clickNewVdrLinkInActions();
		cm.waitForOverlayToHide(); 
		
		VdrDetailsPage vdp = new VdrDetailsPage(driver).initElements();
		vdp.selectKdnNumberFirstVisibleInList(data.get("kdnValidData"));
		
		WorkerSelectionPage wsp= new WorkerSelectionPage(driver).initElements();
		wsp.enterAgentIdAndVerifyAgentDetailsPopup(data.get("agentIdValidData"));
		wsp.selectEnterWorkerDetailsManuallyRadioButton();
		wsp.clickAddWorkers(); 
		wsp.enterPassportNumber(data.get("passportNumberValidData"));
		wsp.clickSearchButtonInPassportEntryWindow();
		wsp.selectWorkerCheckBox();
		wsp.clickSaveButton();  
		wsp.selectSourceCountryEmbassy();	
		wsp.clickOKButton();
		String workersSavedSuccessMessage=vdrApplicationPageProp.getWorkersSavedSuccessMessage();
		cm.waitForStatusBarToBeVisibleAndVerifyTextAndCloseStatusBarAndWaitForOverlayToHIde(workersSavedSuccessMessage);
		String applicationId=wsp.retrieveApplicationId();
		
		hp.clickApplicationStatusInActions();
		ApplicationStatusPage asp=new ApplicationStatusPage(driver).initElements();
		asp.verifySearchVdrByApplicationStatusPageTitle();
		
		asp.searchAllStatusVdrApplications();
		cm.waitForOverlayToHide();
		asp.verifyIfSearchResultsAreDisplayed();
		
		asp.selectSavedStatusFromDropDown();
		cm.waitForOverlayToHide();
		asp.verifyStatusOfVdrApplication(applicationId,"Saved");
		asp.abortSavedVDRApplication(applicationId);
		cm.waitForOverlayToHide();
		
		asp.selectAbortedStatusFromDropDown();
		cm.waitForOverlayToHide();
		asp.sortApplicationId();
		asp.verifyStatusOfVdrApplication(applicationId,"Aborted");
		
		asp.dateValidations();
		
		
		
	}
	

}
