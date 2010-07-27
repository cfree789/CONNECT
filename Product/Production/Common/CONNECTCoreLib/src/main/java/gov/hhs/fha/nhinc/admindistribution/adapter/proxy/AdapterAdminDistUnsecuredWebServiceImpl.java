/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.admindistribution.adapter.proxy;
import oasis.names.tc.emergency.edxl.de._1.EDXLDistribution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.hhs.fha.nhinc.admindistribution.adapter.AdapterAdminDistImpl;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.adapteradmindistribution.AdapterAdministrativeDistributionPortType;
import gov.hhs.fha.nhinc.adapteradmindistribution.AdapterAdministrativeDistribution;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.RespondingGatewaySendAlertMessageType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import javax.xml.ws.BindingProvider;
/**
 *
 * @author dunnek
 */
public class AdapterAdminDistUnsecuredWebServiceImpl implements AdapterAdminDistProxy{
    private Log log = null;
    static AdapterAdministrativeDistribution adapterService = null;
    public AdapterAdminDistUnsecuredWebServiceImpl()
    {
        log = createLogger();
        adapterService = getWebService();
    }
    protected AdapterAdministrativeDistribution getWebService()
    {
        return new AdapterAdministrativeDistribution();
    }
    protected Log createLogger()
    {
        return LogFactory.getLog(getClass());
    }
    public void sendAlertMessage(EDXLDistribution body, AssertionType assertion)
    {
        log.debug("Begin sendAlertMessage");
        String url = null;

        url = getUrl();

        if (NullChecker.isNotNullish(url))
        {
            AdapterAdministrativeDistributionPortType port = getPort(url);
            RespondingGatewaySendAlertMessageType message = new RespondingGatewaySendAlertMessageType();

            message.setEDXLDistribution(body);
            message.setAssertion(assertion);

            gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper.getInstance().initializePort((javax.xml.ws.BindingProvider) port, url);


            port.sendAlertMessage(message);
        }
    }
    protected AdapterAdministrativeDistributionPortType getPort(String url) {
        AdapterAdministrativeDistributionPortType port = adapterService.getAdapterAdministrativeDistributionPortType();

        log.info("Setting endpoint address to Adapter Administrative DIstribution Service to " + url);
        ((BindingProvider) port).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        return port;
    }
    protected String getUrl() {
        String url = null;
        String target = getLocalCommunityId();
        PropertyAccessor props = new PropertyAccessor();

        
        if (target != null) {
            try {
               
                url = ConnectionManagerCache.getEndpointURLByServiceName(target, NhincConstants.ADAPTER_ADMIN_DIST_SERVICE_NAME);
            } catch (ConnectionManagerException ex) {
                log.error("Error: Failed to retrieve url for service: " + NhincConstants.ADAPTER_ADMIN_DIST_SERVICE_NAME);
                log.error(ex.getMessage());
            }
        } else {
            log.error("Target system passed into the proxy is null");
        }

        return url;
    }
    private String getLocalCommunityId()
    {
        PropertyAccessor props = new PropertyAccessor();
        String result = "";
        try
        {
            result = props.getProperty(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.HOME_COMMUNITY_ID_PROPERTY);
        }
        catch(Exception ex)
        {
            log.error("Unable to retrieve local home community id from Gateway.properties");
            log.error(ex);
        }
        return result;
    }

    
}