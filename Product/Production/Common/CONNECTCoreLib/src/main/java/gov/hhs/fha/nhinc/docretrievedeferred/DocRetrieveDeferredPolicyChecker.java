package gov.hhs.fha.nhinc.docretrievedeferred;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.policyengine.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.transform.policy.DocRetrieveDeferredTransformHelper;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Sai Valluripalli
 */
public class DocRetrieveDeferredPolicyChecker {
    private Log log = null;
    private boolean debugEnabled = false;

    /**
     * default constructor
     */
    public DocRetrieveDeferredPolicyChecker()
    {
        log = createLogger();
        debugEnabled = log.isDebugEnabled();
    }

    /**
     *
     * @return Log
     */
    protected Log createLogger()
    {
        return (log != null)?log:LogFactory.getLog(this.getClass());
    }

    /**
     * 
     * @param request
     * @param assertion
     * @param target
     * @return boolean
     */
    public boolean checkOutgoingPolicy (RetrieveDocumentSetResponseType request,AssertionType assertion, String target) {
        if(debugEnabled)
        log.debug("-- Begin DocRetrieveDeferredPolicyChecker.checkOutgoingPolicy() --");
        if(debugEnabled)
        log.debug("checking the policy engine for the new response to a target community");
        DocRetrieveDeferredTransformHelper policyHelper = new DocRetrieveDeferredTransformHelper();
        CheckPolicyRequestType checkPolicyRequest = policyHelper.transformEntityDocRetrieveDeferredRespToCheckPolicy(request, assertion, target);
        
        if(debugEnabled)
        log.debug("-- End DocRetrieveDeferredPolicyChecker.checkOutgoingPolicy() --");
        return invokePolicyEngine(checkPolicyRequest);
    }

    /**
     * 
     * @param request
     * @param assertion
     * @return boolean
     */
    public boolean checkIncomingPolicy (RetrieveDocumentSetResponseType request, AssertionType assertion) {
        if(debugEnabled)
            log.debug("-- Begin DocRetrieveDeferredPolicyChecker.checkIncomingPolicy() --");
        if(debugEnabled)
        log.debug("checking the policy engine for the new request to a target community");
        DocRetrieveDeferredTransformHelper policyHelper;
        policyHelper = new DocRetrieveDeferredTransformHelper();
        CheckPolicyRequestType checkPolicyRequest = policyHelper.transformNhinDocRetrieveDeferredRespToCheckPolicy(request, assertion);
        if(debugEnabled)
            log.debug("-- End DocRetrieveDeferredPolicyChecker.checkIncomingPolicy() --");
        return invokePolicyEngine(checkPolicyRequest);
    }

    /**
     * 
     * @param policyCheckReq
     * @return boolean
     */
    protected boolean invokePolicyEngine(CheckPolicyRequestType policyCheckReq) {
        if(debugEnabled)
            log.debug("Begin DocRetrieveDeferredPolicyChecker.invokePolicyEngine");
        boolean policyIsValid = false;
        if(debugEnabled)
        log.debug("start invokePolicyEngine");
         /* invoke check policy */
        PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
        PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
        CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyCheckReq);

        /* if response='permit' */
        if (policyResp.getResponse() != null &&
                NullChecker.isNotNullish(policyResp.getResponse().getResult()) &&
                policyResp.getResponse().getResult().get(0).getDecision() == DecisionType.PERMIT) {
            if(debugEnabled)
            log.debug("Policy engine check returned permit.");
            policyIsValid = true;
        } else {
            if(debugEnabled)
            log.debug("Policy engine check returned deny.");
            policyIsValid = false;
        }
        if(debugEnabled)
            log.debug("End DocRetrieveDeferredPolicyChecker.invokePolicyEngine");
        return policyIsValid;
    }
}