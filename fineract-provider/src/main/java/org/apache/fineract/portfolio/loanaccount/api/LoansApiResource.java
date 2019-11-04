package org.apache.fineract.portfolio.loanaccount.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.ReceiptNullException;
import org.apache.fineract.portfolio.loanaccount.exception.ReceiptNumberExistException;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;

@Path("/loans")
@Component
@Scope("singleton")
public class LoansApiResource
{
  private final Set<String> LOAN_DATA_PARAMETERS = new HashSet(Arrays.asList(new String[] { "id", "accountNo", "status", "externalId", "clientId", "group", "loanProductId", "loanProductName", "loanProductDescription", "isLoanProductLinkedToFloatingRate", "fundId", "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal", "totalOverpaid", "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery", "interestRatePerPeriod", "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyId", "transactionProcessingStrategyName", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType", "allowPartialPeriodInterestCalcualtion", "expectedFirstRepaymentOnDate", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment", "graceOnInterestCharged", "interestChargedFromDate", "timeline", "totalFeeChargesAtDisbursement", "summary", "repaymentSchedule", "transactions", "charges", "collateral", "guarantors", "meeting", "productOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions", "repaymentFrequencyNthDayTypeOptions", "repaymentFrequencyDaysOfWeekTypeOptions", "termFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerOptions", "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions", "syncDisbursementWithMeeting", "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount", "interestRateDifferential", "isFloatingInterestRate", "interestRatesPeriods", "isSecure", "canUseForTopup", "isTopup", "loanIdToClose", "topupAmount", "clientActiveLoanOptions", "datatables" }));
  private final Set<String> LOAN_APPROVAL_DATA_PARAMETERS = new HashSet(Arrays.asList(new String[] { "approvalDate", "approvalAmount" }));
  private final String resourceNameForPermissions = "LOAN";
  private final PlatformSecurityContext context;
  private final LoanReadPlatformService loanReadPlatformService;
  private final LoanProductReadPlatformService loanProductReadPlatformService;
  private final LoanDropdownReadPlatformService dropdownReadPlatformService;
  private final FundReadPlatformService fundReadPlatformService;
  private final ChargeReadPlatformService chargeReadPlatformService;
  private final LoanChargeReadPlatformService loanChargeReadPlatformService;
  private final CollateralReadPlatformService loanCollateralReadPlatformService;
  private final LoanScheduleCalculationPlatformService calculationPlatformService;
  private final GuarantorReadPlatformService guarantorReadPlatformService;
  private final CodeValueReadPlatformService codeValueReadPlatformService;
  private final GroupReadPlatformService groupReadPlatformService;
  private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
  private final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer;
  private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
  private final ApiRequestParameterHelper apiRequestParameterHelper;
  private final FromJsonHelper fromJsonHelper;
  private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
  private final CalendarReadPlatformService calendarReadPlatformService;
  private final NoteReadPlatformServiceImpl noteReadPlatformService;
  private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
  private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
  private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
  private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
  private final EntityDatatableChecksReadService entityDatatableChecksReadService;
  private final BulkImportWorkbookService bulkImportWorkbookService;
  private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
  
  @Autowired
  public LoansApiResource(PlatformSecurityContext context, LoanReadPlatformService loanReadPlatformService, LoanProductReadPlatformService loanProductReadPlatformService, LoanDropdownReadPlatformService dropdownReadPlatformService, FundReadPlatformService fundReadPlatformService, ChargeReadPlatformService chargeReadPlatformService, LoanChargeReadPlatformService loanChargeReadPlatformService, CollateralReadPlatformService loanCollateralReadPlatformService, LoanScheduleCalculationPlatformService calculationPlatformService, GuarantorReadPlatformService guarantorReadPlatformService, CodeValueReadPlatformService codeValueReadPlatformService, GroupReadPlatformService groupReadPlatformService, DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer, DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer, DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer, ApiRequestParameterHelper apiRequestParameterHelper, FromJsonHelper fromJsonHelper, PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, CalendarReadPlatformService calendarReadPlatformService, NoteReadPlatformServiceImpl noteReadPlatformService, PortfolioAccountReadPlatformService portfolioAccountReadPlatformServiceImpl, AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService, AccountDetailsReadPlatformService accountDetailsReadPlatformService, EntityDatatableChecksReadService entityDatatableChecksReadService, BulkImportWorkbookService bulkImportWorkbookService, BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService)
  {
    this.context = context;
    this.loanReadPlatformService = loanReadPlatformService;
    this.loanProductReadPlatformService = loanProductReadPlatformService;
    this.dropdownReadPlatformService = dropdownReadPlatformService;
    this.fundReadPlatformService = fundReadPlatformService;
    this.chargeReadPlatformService = chargeReadPlatformService;
    this.loanChargeReadPlatformService = loanChargeReadPlatformService;
    this.loanCollateralReadPlatformService = loanCollateralReadPlatformService;
    this.calculationPlatformService = calculationPlatformService;
    this.guarantorReadPlatformService = guarantorReadPlatformService;
    this.codeValueReadPlatformService = codeValueReadPlatformService;
    this.groupReadPlatformService = groupReadPlatformService;
    this.toApiJsonSerializer = toApiJsonSerializer;
    this.loanApprovalDataToApiJsonSerializer = loanApprovalDataToApiJsonSerializer;
    this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
    this.apiRequestParameterHelper = apiRequestParameterHelper;
    this.fromJsonHelper = fromJsonHelper;
    this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    this.calendarReadPlatformService = calendarReadPlatformService;
    this.noteReadPlatformService = noteReadPlatformService;
    portfolioAccountReadPlatformService = portfolioAccountReadPlatformServiceImpl;
    this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
    this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
    this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
    this.entityDatatableChecksReadService = entityDatatableChecksReadService;
    this.bulkImportWorkbookService = bulkImportWorkbookService;
    this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
  }
  
  @GET
  @Path("{loanId}/template")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String retrieveApprovalTemplate(@PathParam("loanId") Long loanId, @QueryParam("templateType") String templateType, @Context UriInfo uriInfo)
  {
    getClass();context.authenticatedUser().validateHasReadPermission("LOAN");
    
    LoanApprovalData loanApprovalTemplate = null;
    if (templateType == null)
    {
      String errorMsg = "Loan template type must be provided";
      throw new LoanTemplateTypeRequiredException("Loan template type must be provided");
    }
    if (templateType.equals("approval")) {
      loanApprovalTemplate = loanReadPlatformService.retrieveApprovalTemplate(loanId);
    }
    ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
    return loanApprovalDataToApiJsonSerializer.serialize(settings, loanApprovalTemplate, LOAN_APPROVAL_DATA_PARAMETERS);
  }
  
  @GET
  @Path("template")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String template(@QueryParam("clientId") Long clientId, @QueryParam("groupId") Long groupId, @QueryParam("productId") Long productId, @QueryParam("templateType") String templateType, @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") boolean staffInSelectedOfficeOnly, @DefaultValue("false") @QueryParam("activeOnly") boolean onlyActive, @Context UriInfo uriInfo)
  {
    getClass();context.authenticatedUser().validateHasReadPermission("LOAN");
    
    Collection<LoanProductData> productOptions = loanProductReadPlatformService.retrieveAllLoanProductsForLookup(onlyActive);
    
    Collection<StaffData> allowedLoanOfficers = null;
    Collection<CodeValueData> loanCollateralOptions = null;
    Collection<CalendarData> calendarOptions = null;
    LoanAccountData newLoanAccount = null;
    Long officeId = null;
    Collection<PortfolioAccountData> accountLinkingOptions = null;
    if (productId != null) {
      newLoanAccount = loanReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId);
    }
    if (templateType == null)
    {
      String errorMsg = "Loan template type must be provided";
      throw new LoanTemplateTypeRequiredException("Loan template type must be provided");
    }
    if (templateType.equals("collateral"))
    {
      loanCollateralOptions = codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
      newLoanAccount = LoanAccountData.collateralTemplate(loanCollateralOptions);
    }
    else
    {
      if ((templateType.equals("individual")) || (templateType.equals("jlg")))
      {
        if (clientId == null)
        {
          newLoanAccount = newLoanAccount == null ? LoanAccountData.emptyTemplate() : newLoanAccount;
        }
        else
        {
          LoanAccountData loanAccountClientDetails = loanReadPlatformService.retrieveClientDetailsTemplate(clientId);
          
          officeId = loanAccountClientDetails.officeId();
          newLoanAccount = newLoanAccount == null ? loanAccountClientDetails : LoanAccountData.populateClientDefaults(newLoanAccount, loanAccountClientDetails);
        }
        if (templateType.equals("jlg"))
        {
          GroupGeneralData group = groupReadPlatformService.retrieveOne(groupId);
          newLoanAccount = LoanAccountData.associateGroup(newLoanAccount, group);
          calendarOptions = loanReadPlatformService.retrieveCalendars(groupId);
        }
      }
      else if (templateType.equals("group"))
      {
        LoanAccountData loanAccountGroupData = loanReadPlatformService.retrieveGroupDetailsTemplate(groupId);
        officeId = loanAccountGroupData.groupOfficeId();
        calendarOptions = loanReadPlatformService.retrieveCalendars(groupId);
        newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount, loanAccountGroupData);
        
        accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);
      }
      else if (templateType.equals("jlgbulk"))
      {
        LoanAccountData loanAccountGroupData = loanReadPlatformService.retrieveGroupAndMembersDetailsTemplate(groupId);
        officeId = loanAccountGroupData.groupOfficeId();
        calendarOptions = loanReadPlatformService.retrieveCalendars(groupId);
        newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount, loanAccountGroupData);
        if (productId != null)
        {
          Map<Long, Integer> memberLoanCycle = new HashMap();
          Collection<ClientData> members = loanAccountGroupData.groupData().clientMembers();
          accountLinkingOptions = new ArrayList();
          if (members != null) {
            for (ClientData clientData : members)
            {
              Integer loanCounter = loanReadPlatformService.retriveLoanCounter(clientData.id(), productId);
              memberLoanCycle.put(clientData.id(), loanCounter);
              accountLinkingOptions.addAll(getaccountLinkingOptions(newLoanAccount, clientData.id(), groupId));
            }
          }
          newLoanAccount = LoanAccountData.associateMemberVariations(newLoanAccount, memberLoanCycle);
        }
      }
      else
      {
        String errorMsg = "Loan template type '" + templateType + "' is not supported";
        throw new NotSupportedLoanTemplateTypeException(errorMsg, new Object[] { templateType });
      }
      allowedLoanOfficers = loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);
      if (clientId != null) {
        accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);
      }
      newLoanAccount = LoanAccountData.associationsAndTemplate(newLoanAccount, productOptions, allowedLoanOfficers, calendarOptions, accountLinkingOptions);
    }
    List<DatatableData> datatableTemplates = entityDatatableChecksReadService.retrieveTemplates(Long.valueOf(StatusEnum.CREATE.getCode().longValue()), EntityTables.LOAN.getName(), productId);
    newLoanAccount.setDatatables(datatableTemplates);
    
    ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
    return toApiJsonSerializer.serialize(settings, newLoanAccount, LOAN_DATA_PARAMETERS);
  }
  
  private Collection<PortfolioAccountData> getaccountLinkingOptions(LoanAccountData newLoanAccount, Long clientId, Long groupId)
  {
    CurrencyData currencyData = newLoanAccount.currency();
    String currencyCode = null;
    if (currencyData != null) {
      currencyCode = currencyData.code();
    }
    long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue().intValue() };
    
    PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId, currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
    if (groupId != null) {
      portfolioAccountDTO.setGroupId(groupId);
    }
    return portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
  }
  
  @GET
  @Path("{loanId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String retrieveLoan(@PathParam("loanId") Long loanId, @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") boolean staffInSelectedOfficeOnly, @Context UriInfo uriInfo)
  {
    long start = System.currentTimeMillis();
    getClass();context.authenticatedUser().validateHasReadPermission("LOAN");
    
    LoanAccountData loanBasicDetails = loanReadPlatformService.retrieveOne(loanId);
    if (loanBasicDetails.isInterestRecalculationEnabled())
    {
      Collection<CalendarData> interestRecalculationCalendarDatas = calendarReadPlatformService.retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL
        .getValue(), null);
      CalendarData calendarData = null;
      if (!CollectionUtils.isEmpty(interestRecalculationCalendarDatas)) {
        calendarData = (CalendarData)interestRecalculationCalendarDatas.iterator().next();
      }
      Collection<CalendarData> interestRecalculationCompoundingCalendarDatas = calendarReadPlatformService.retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL
        .getValue(), null);
      CalendarData compoundingCalendarData = null;
      if (!CollectionUtils.isEmpty(interestRecalculationCompoundingCalendarDatas)) {
        compoundingCalendarData = (CalendarData)interestRecalculationCompoundingCalendarDatas.iterator().next();
      }
      loanBasicDetails = LoanAccountData.withInterestRecalculationCalendarData(loanBasicDetails, calendarData, compoundingCalendarData);
    }
    if (loanBasicDetails.isMonthlyRepaymentFrequencyType())
    {
      Collection<CalendarData> loanCalendarDatas = calendarReadPlatformService.retrieveCalendarsByEntity(loanId, CalendarEntityType.LOANS
        .getValue(), null);
      CalendarData calendarData = null;
      if (!CollectionUtils.isEmpty(loanCalendarDatas)) {
        calendarData = (CalendarData)loanCalendarDatas.iterator().next();
      }
      if (calendarData != null) {
        loanBasicDetails = LoanAccountData.withLoanCalendarData(loanBasicDetails, calendarData);
      }
    }
    Collection<InterestRatePeriodData> interestRatesPeriods = loanReadPlatformService.retrieveLoanInterestRatePeriodData(loanBasicDetails);
    Collection<LoanTransactionData> loanRepayments = null;
    LoanScheduleData repaymentSchedule = null;
    Collection<LoanChargeData> charges = null;
    Collection<GuarantorData> guarantors = null;
    Collection<CollateralData> collateral = null;
    CalendarData meeting = null;
    Collection<NoteData> notes = null;
    PortfolioAccountData linkedAccount = null;
    Collection<DisbursementData> disbursementData = null;
    Collection<LoanTermVariationsData> emiAmountVariations = null;
    
    Set<String> mandatoryResponseParameters = new HashSet();
    Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
    if (!associationParameters.isEmpty())
    {
      if (associationParameters.contains("all")) {
        associationParameters.addAll(Arrays.asList(new String[] { "repaymentSchedule", "futureSchedule", "originalSchedule", "transactions", "charges", "guarantors", "collateral", "notes", "linkedAccount", "multiDisburseDetails" }));
      }
      ApiParameterHelper.excludeAssociationsForResponseIfProvided(uriInfo.getQueryParameters(), associationParameters);
      if (associationParameters.contains("guarantors"))
      {
        mandatoryResponseParameters.add("guarantors");
        guarantors = guarantorReadPlatformService.retrieveGuarantorsForLoan(loanId);
        if (CollectionUtils.isEmpty(guarantors)) {
          guarantors = null;
        }
      }
      if (associationParameters.contains("transactions"))
      {
        mandatoryResponseParameters.add("transactions");
        Collection<LoanTransactionData> currentLoanRepayments = loanReadPlatformService.retrieveLoanTransactions(loanId);
        if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
          loanRepayments = currentLoanRepayments;
        }
      }
      if ((associationParameters.contains("multiDisburseDetails")) || (associationParameters.contains("repaymentSchedule")))
      {
        mandatoryResponseParameters.add("multiDisburseDetails");
        disbursementData = loanReadPlatformService.retrieveLoanDisbursementDetails(loanId);
      }
      if ((associationParameters.contains("emiAmountVariations")) || (associationParameters.contains("repaymentSchedule")))
      {
        mandatoryResponseParameters.add("emiAmountVariations");
        emiAmountVariations = loanReadPlatformService.retrieveLoanTermVariations(loanId, LoanTermVariationType.EMI_AMOUNT
          .getValue());
      }
      if (associationParameters.contains("repaymentSchedule"))
      {
        mandatoryResponseParameters.add("repaymentSchedule");
        RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = loanBasicDetails.repaymentScheduleRelatedData();
        repaymentSchedule = loanReadPlatformService.retrieveRepaymentSchedule(loanId, repaymentScheduleRelatedData, disbursementData, loanBasicDetails
          .isInterestRecalculationEnabled(), loanBasicDetails.getTotalPaidFeeCharges());
        if ((associationParameters.contains("futureSchedule")) && (loanBasicDetails.isInterestRecalculationEnabled()))
        {
          mandatoryResponseParameters.add("futureSchedule");
          calculationPlatformService.updateFutureSchedule(repaymentSchedule, loanId);
        }
        if ((associationParameters.contains("originalSchedule")) && (loanBasicDetails.isInterestRecalculationEnabled()) && 
          (loanBasicDetails.isActive()))
        {
          mandatoryResponseParameters.add("originalSchedule");
          LoanScheduleData loanScheduleData = loanScheduleHistoryReadPlatformService.retrieveRepaymentArchiveSchedule(loanId, repaymentScheduleRelatedData, disbursementData);
          
          loanBasicDetails = LoanAccountData.withOriginalSchedule(loanBasicDetails, loanScheduleData);
        }
      }
      if (associationParameters.contains("charges"))
      {
        mandatoryResponseParameters.add("charges");
        charges = loanChargeReadPlatformService.retrieveLoanCharges(loanId);
        if (CollectionUtils.isEmpty(charges)) {
          charges = null;
        }
      }
      if (associationParameters.contains("collateral"))
      {
        mandatoryResponseParameters.add("collateral");
        collateral = loanCollateralReadPlatformService.retrieveCollaterals(loanId);
        if (CollectionUtils.isEmpty(collateral)) {
          collateral = null;
        }
      }
      if (associationParameters.contains("meeting"))
      {
        mandatoryResponseParameters.add("meeting");
        meeting = calendarReadPlatformService.retrieveLoanCalendar(loanId);
      }
      if (associationParameters.contains("notes"))
      {
        mandatoryResponseParameters.add("notes");
        notes = noteReadPlatformService.retrieveNotesByResource(loanId, NoteType.LOAN.getValue());
        if (CollectionUtils.isEmpty(notes)) {
          notes = null;
        }
      }
      if (associationParameters.contains("linkedAccount"))
      {
        mandatoryResponseParameters.add("linkedAccount");
        linkedAccount = accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
      }
    }
    Collection<LoanProductData> productOptions = null;
    LoanProductData product = null;
    Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
    Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
    Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
    Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
    Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
    Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
    Collection<EnumOptionData> amortizationTypeOptions = null;
    Collection<EnumOptionData> interestTypeOptions = null;
    Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
    Collection<FundData> fundOptions = null;
    Collection<StaffData> allowedLoanOfficers = null;
    Collection<ChargeData> chargeOptions = null;
    ChargeData chargeTemplate = null;
    Collection<CodeValueData> loanPurposeOptions = null;
    Collection<CodeValueData> loanCollateralOptions = null;
    Collection<CalendarData> calendarOptions = null;
    Collection<PortfolioAccountData> accountLinkingOptions = null;
    PaidInAdvanceData paidInAdvanceTemplate = null;
    Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;
    
    boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
    if (template)
    {
      productOptions = loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
      product = loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.loanProductId());
      loanBasicDetails.setProduct(product);
      loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
      repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
      repaymentFrequencyNthDayTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
      repaymentFrequencyDayOfWeekTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForDaysOfWeek();
      interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();
      
      amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
      if (product.isLinkedToFloatingInterestRates()) {
        interestTypeOptions = Arrays.asList(new EnumOptionData[] { LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE) });
      } else {
        interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
      }
      interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
      
      fundOptions = fundReadPlatformService.retrieveAllFunds();
      repaymentStrategyOptions = dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
      if (product.getMultiDisburseLoan().booleanValue()) {
        chargeOptions = chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId, new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
      } else {
        chargeOptions = chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId, new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT });
      }
      chargeTemplate = loanChargeReadPlatformService.retrieveLoanChargeTemplate();
      
      allowedLoanOfficers = loanReadPlatformService.retrieveAllowedLoanOfficers(loanBasicDetails.officeId(), staffInSelectedOfficeOnly);
      
      loanPurposeOptions = codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
      loanCollateralOptions = codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
      CurrencyData currencyData = loanBasicDetails.currency();
      String currencyCode = null;
      if (currencyData != null) {
        currencyCode = currencyData.code();
      }
      long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue().intValue() };
      
      PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), loanBasicDetails.clientId(), currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
      accountLinkingOptions = portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
      if (!associationParameters.contains("linkedAccount"))
      {
        mandatoryResponseParameters.add("linkedAccount");
        linkedAccount = accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
      }
      if (loanBasicDetails.groupId() != null) {
        calendarOptions = loanReadPlatformService.retrieveCalendars(loanBasicDetails.groupId());
      }
      if ((loanBasicDetails.product().canUseForTopup()) && (loanBasicDetails.clientId() != null)) {
        clientActiveLoanOptions = accountDetailsReadPlatformService.retrieveClientActiveLoanAccountSummary(loanBasicDetails.clientId());
      }
    }
    Collection<ChargeData> overdueCharges = chargeReadPlatformService.retrieveLoanProductCharges(loanBasicDetails.loanProductId(), ChargeTimeType.OVERDUE_INSTALLMENT);
    
    paidInAdvanceTemplate = loanReadPlatformService.retrieveTotalPaidInAdvance(loanId);
    
    LoanAccountData loanAccount = LoanAccountData.associationsAndTemplate(loanBasicDetails, repaymentSchedule, loanRepayments, charges, collateral, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions, repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions, loanCollateralOptions, calendarOptions, notes, accountLinkingOptions, linkedAccount, disbursementData, emiAmountVariations, overdueCharges, paidInAdvanceTemplate, interestRatesPeriods, clientActiveLoanOptions);
    
    ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters(), mandatoryResponseParameters);
    
    String toReturn = toApiJsonSerializer.serialize(settings, loanAccount, LOAN_DATA_PARAMETERS);
    return toReturn;
  }
  
  @GET
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String retrieveAll(@Context UriInfo uriInfo, @QueryParam("sqlSearch") String sqlSearch, @QueryParam("externalId") String externalId, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("orderBy") String orderBy, @QueryParam("sortOrder") String sortOrder, @QueryParam("accountNo") String accountNo)
  {
    getClass();context.authenticatedUser().validateHasReadPermission("LOAN");
    
    SearchParameters searchParameters = SearchParameters.forLoans(sqlSearch, externalId, offset, limit, orderBy, sortOrder, accountNo);
    
    Page<LoanAccountData> loanBasicDetails = loanReadPlatformService.retrieveAll(searchParameters);
    
    ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
    return toApiJsonSerializer.serialize(settings, loanBasicDetails, LOAN_DATA_PARAMETERS);
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String calculateLoanScheduleOrSubmitLoanApplication(@QueryParam("command") String commandParam, @Context UriInfo uriInfo, String apiRequestBodyAsJson)
  {
    if (is(commandParam, "calculateLoanSchedule"))
    {
      JsonElement parsedQuery = fromJsonHelper.parse(apiRequestBodyAsJson);
      JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, fromJsonHelper);
      
      LoanScheduleModel loanSchedule = calculationPlatformService.calculateLoanSchedule(query, Boolean.valueOf(true));
      
      ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
      return loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule.toData(), new HashSet());
    }
    CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanApplication().withJson(apiRequestBodyAsJson).build();
    
    CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    
    return toApiJsonSerializer.serialize(result);
  }
  
  @PUT
  @Path("{loanId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String modifyLoanApplication(@PathParam("loanId") Long loanId, String apiRequestBodyAsJson)
  {
    CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanApplication(loanId).withJson(apiRequestBodyAsJson).build();
    
    CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    
    return toApiJsonSerializer.serialize(result);
  }
  
  @DELETE
  @Path("{loanId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String deleteLoanApplication(@PathParam("loanId") Long loanId)
  {
    CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanApplication(loanId).build();
    
    CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    
    return toApiJsonSerializer.serialize(result);
  }
  
  @POST
  @Path("{loanId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  public String stateTransitions(@PathParam("loanId") Long loanId, @QueryParam("command") String commandParam,String apiRequestBodyAsJson)
  {
    CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
  
	
  
    CommandProcessingResult result = null;
    if (is(commandParam, "reject"))
    {
      CommandWrapper commandRequest = builder.rejectLoanApplication(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "withdrawnByApplicant"))
    {
      CommandWrapper commandRequest = builder.withdrawLoanApplication(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "approve"))
    {
      CommandWrapper commandRequest = builder.approveLoanApplication(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "disburse"))    {
//      BuildOptions a = new BuildOptions();
//      JsonParser ps = new JsonParser();
//      JsonElement js = ps.parse(apiRequestBodyAsJson);
//      JsonObject jsonObject = js.getAsJsonObject();
//      
//      JsonElement pay = jsonObject.get("paymentTypeId");
//      if ("".equals(pay.getAsString().replaceAll("\\W", ""))) {
//        throw new ReceiptNullException();
//      }
//      if (pay.getAsInt() == 4)
//      {
//        JsonElement r = jsonObject.get("receiptNumber");
//        if ("".equals(r.getAsString().replaceAll("\\W", ""))) {
//          throw new ReceiptNullException();
//        }
//        try
//        {
//          JsonParser p = new JsonParser();
//          JsonElement j = p.parse(a.checkReceipt(r.toString(),Identifier));
//          JsonObject job = j.getAsJsonObject();
//          JsonElement resp = job.get("message");
//          if (!resp.getAsBoolean()) {
//            throw new ReceiptNumberExistException(r.toString());
//          }
          CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
          result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
//        }
//        catch (JsonSyntaxException|IOException e)
//        {
//          e.printStackTrace();
//        }
//      }
//      else
//      {
      //  CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
     //   result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
      //}
    }
    else if (is(commandParam, "disburseToSavings"))
    {
      CommandWrapper commandRequest = builder.disburseLoanToSavingsApplication(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    if (is(commandParam, "undoapproval"))
    {
      CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "undodisbursal"))
    {
      CommandWrapper commandRequest = builder.undoLoanApplicationDisbursal(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "undolastdisbursal"))
    {
      CommandWrapper commandRequest = builder.undoLastDisbursalLoanApplication(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    if (is(commandParam, "assignloanofficer"))
    {
      CommandWrapper commandRequest = builder.assignLoanOfficer(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "unassignloanofficer"))
    {
      CommandWrapper commandRequest = builder.unassignLoanOfficer(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    else if (is(commandParam, "recoverGuarantees"))
    {
      CommandWrapper commandRequest = new CommandWrapperBuilder().recoverFromGuarantor(loanId).build();
      result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    if (result == null) {
      throw new UnrecognizedQueryParamException("command", commandParam, new Object[0]);
    }
    return toApiJsonSerializer.serialize(result);
  }
  
  private boolean is(String commandParam, String commandValue)
  {
    return (StringUtils.isNotBlank(commandParam)) && (commandParam.trim().equalsIgnoreCase(commandValue));
  }
  
  @GET
  @Path("downloadtemplate")
  @Produces({"application/vnd.ms-excel"})
  public Response getLoansTemplate(@QueryParam("officeId") Long officeId, @QueryParam("staffId") Long staffId, @QueryParam("dateFormat") String dateFormat)
  {
    return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOANS.toString(), officeId, staffId, dateFormat);
  }
  
  @GET
  @Path("repayments/downloadtemplate")
  @Produces({"application/vnd.ms-excel"})
  public Response getLoanRepaymentTemplate(@QueryParam("officeId") Long officeId, @QueryParam("dateFormat") String dateFormat)
  {
    return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOAN_TRANSACTIONS.toString(), officeId, null, dateFormat);
  }
  
  @POST
  @Path("uploadtemplate")
  @Consumes({"multipart/form-data"})
  public String postLoanTemplate(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") String locale, @FormDataParam("dateFormat") String dateFormat)
  {
    Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOANS.toString(), uploadedInputStream, fileDetail, locale, dateFormat);
    return toApiJsonSerializer.serialize(importDocumentId);
  }
  
  @POST
  @Path("repayments/uploadtemplate")
  @Consumes({"multipart/form-data"})
  public String postLoanRepaymentTemplate(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") String locale, @FormDataParam("dateFormat") String dateFormat)
  {
    Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOAN_TRANSACTIONS.toString(), uploadedInputStream, fileDetail, locale, dateFormat);
    
    return toApiJsonSerializer.serialize(importDocumentId);
  }
}

/* Location:
 * Qualified Name:     org.apache.fineract.portfolio.loanaccount.api.LoansApiResource
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */