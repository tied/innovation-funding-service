*** Settings ***
Documentation     IFS-7313  New completion stage for Procurement - Comp setup journey
...
...               IFS-7314  New completion stage for Procurement - applicant journey
...
...               IFS-7315  New completion stage- comp transfers to 'previous'
...
Suite Setup       Custom Suite Setup
Suite Teardown    Custom suite teardown
Force Tags        CompAdmin
Resource          ../../../resources/defaultResources.robot
Resource          ../../../resources/common/Applicant_Commons.robot
Resource          ../../../resources/common/Competition_Commons.robot
Resource          ../../../resources/common/PS_Common.robot

*** Variables ***
${sbriType1ApplicationTitle}     SBRI type one application
${sbriType1CompetitionName}      SBRI Type 1 Competition
${openSBRICompetitionName}       SBRI type one competition
${openSBRICompetitionId}         ${competition_ids["${openSBRICompetitionName}"]}
&{sbriLeadCredentials}           email=troy.ward@gmail.com     password=${short_password}
&{sbriPartnerCredentials}        email=eve.smith@gmail.com     password=${short_password}
${sbriProjectName}               Procurement application 1
${sbriProjectId}                 ${project_ids["${sbriProjectName}"]}

*** Test Cases ***
Comp admin saves the completition stage with competition close option
    [Documentation]  IFS-7313
    Given the user completes initial details of the competition        ${sbriType1CompetitionName}  PROCUREMENT
    When the user navigates to completition stage
    And the user saves the completion stage with competition close     COMPETITION_CLOSE
    Then the user should see competition close in read only page       Competition close

Comp admin edits the completition stage with competition close option
    [Documentation]  IFS-7313
    Given the user clicks the button/link                               jQuery = button:contains("Edit")
    When the user saves the completion stage with competition close     COMPETITION_CLOSE
    Then the user should see competition close in read only page        Competition close

Comp admin complete the SBRI milestones
    [Documentation]  IFS-7313
    Given the user clicks the button/link                                  jQuery = span:contains("Milestones")
    When the user fills in the competition close Milestones
    Then the user should see the correct inputs in the Milestones form
    And the user should see milestones section marked as complete

Awaiting assessment status should not display for SBRI submitted applications
    [Documentation]  IFS-7314
    When log in as a different user              &{sbriLeadCredentials}
    Then the user should not see the element     jQuery = .in-progress li:contains("${sbriType1ApplicationTitle}") .msg-deadline-waiting:contains("Awaiting assessment")
    And the user should see the element          jQuery = .in-progress li:contains("${sbriType1ApplicationTitle}") .msg-progress:contains("Application submitted")

Innovation Lead should only see key statistics and competition navigation links related to applications when the SBRI competition is open
    [Documentation]  IFS-7315
    Given log in as a different user                                                 &{innovation_lead_one}
    When the user navigates to the page                                              ${server}/management/competition/${openSBRICompetitionId}
    Then the user should only see application related key statistics
    And the user should only see application related competition navigation links

Innovation Lead should only see competition close completion milestones when the SBRI competition is open
    [Documentation]  IFS-7315
    Then the user should only see competition close completion stage milestones

Innovation Lead should only see application related key statistics in applications page when the SBRI competition is open
    [Documentation]  IFS-7315
    When the user clicks the button/link                                                     link = Applications: Submitted, ineligible
    Then the user should only see application related key statistics in applications page

Stakeholder should only see key statistics and competition navigation links related to applications when the SBRI competition is open
    [Documentation]  IFS-7315
    Given the user assign the stakeholder to the SBRI competition
    And log in as a different user                                                    &{stakeholder_user}
    When the user navigates to the page                                               ${server}/management/competition/${openSBRICompetitionId}
    Then the user should only see application related key statistics
    And the user should only see application related competition navigation links

Stakeholder should only see competition close completion milestones when the SBRI competition is open
    [Documentation]  IFS-7315
    Then the user should only see competition close completion stage milestones

Stakeholder should only see application related key statistics in applications page when the SBRI competition is open
    [Documentation]  IFS-7315
    When the user clicks the button/link                                                     link = Applications: Submitted, ineligible
    Then the user should only see application related key statistics in applications page

Internal users should only see key statistics and competition navigation links related to applications when the SBRI competition is open
    [Documentation]  IFS-7315
    Given log in as a different user                                                 &{internal_finance_credentials}
    When the user navigates to the page                                              ${server}/management/competition/${openSBRICompetitionId}
    Then the user should only see application related key statistics
    And the user should only see application related competition navigation links

Internal users should only see competition close completion milestones when the SBRI competition is open
    [Documentation]  IFS-7315
    Then the user should only see competition close completion stage milestones

Internal users should only see application related key statistics in applications page when the SBRI competition is open
    [Documentation]  IFS-7315
    When the user clicks the button/link                                                     link = Applications: All, submitted, ineligible
    Then the user should only see application related key statistics in applications page

Lead applicant can see SBRI applications in previous section when the competition is closed
    [Documentation]  IFS-7314
    Given moving competition to Closed        ${openSBRICompetitionId}
    When log in as a different user           &{sbriLeadCredentials}
    Then the user should see the element      jQuery = .previous li:contains("${sbriType1ApplicationTitle}") .msg-progress:contains("Application submitted")

Partner applicant can see SBRI applications in previous section when the competition is closed
    [Documentation]  IFS-7314
    When log in as a different user           &{sbriPartnerCredentials}
    Then the user should see the element      jQuery = .previous li:contains("${sbriType1ApplicationTitle}") .msg-progress:contains("Application submitted")

Internal users can see SBRI competition in previous tab
    [Documentation]  IFS-7315
    Given log in as a different user         &{ifs_admin_user_credentials}
    When the user clicks the button/link     jQuery = a:contains("Previous")
    Then the user should see the element     link = ${openSBRICompetitionName}

Internal users can see SBRI application in previous tab with submitted status
    [Documentation]  IFS-7315
    Given the user navigates to the page      ${server}/management/competition/${openSBRICompetitionId}/previous
    When the user clicks the button/link      id = accordion-previous-heading-2
    Then the user should see the element      jQuery = td:contains("Submitted")

Internal user finance checks page
    [Documentation]    IFS-8127
    [Setup]  Log in as a different user           &{internal_finance_credentials}
    Given the user navigates to the page          ${server}/project-setup-management/project/${sbriProjectId}/finance-check
    Then the user should see the correct data on finance check page

Internal user eligibility page
    [Documentation]    IFS-8127
     Given the user clicks the button/link        css = .eligibility-0
     Then the user should not see the element     jQuery = h2:contains("Finances overview")
     And the user should not see the element      link = Review all changes to project finances

Internal user can set VAT to no
     Given The user clicks the button/link        jQuery = div:contains("Are you VAT registered") ~ div a:contains("Edit")
     When the user selects the radio button        vatForm.registered  false
     And The user clicks the button/link          jQuery = div:contains("Total project costs inclusive of VAT") ~ div button:contains("Save")
     Then the user should see calculations without VAT

Internal user can set VAT to yes
     Given the user clicks the button/link        jQuery = div:contains("Are you VAT registered") ~ div a:contains("Edit")
     When the user selects the radio button        vatForm.registered  true
     And The user clicks the button/link          jQuery = div:contains("Total project costs inclusive of VAT") ~ div button:contains("Save")
     Then the user should see calculations with VAT

Internal user viability page
    [Documentation]    IFS-8127
    Given the user clicks the button/link        link = Finance checks
    When the user clicks the button/link         css = .viability-0
    Then the user should not see the element     css = .table-overview

External user finance overview link is not show
    [Documentation]    IFS-8127
    Given log in as a different user             &{becky_mason_credentials}
    When the user navigates to the page          ${server}/project-setup/project/${sbriProjectId}/finance-checks
    Then the user should not see the element     jQuery = project finance overview

External user finances
    [Documentation]    IFS-8127
    Given the user clicks the button/link        link = your project finances
    Then the user should not see the element     link = View changes to finances
    And the user should not see the element       css = table-overview
    And the external user should see the correct VAT information

*** Keywords ***
Custom Suite Setup
    Connect to Database  @{database}
    The user logs-in in new browser     &{Comp_admin1_credentials}
    Set predefined date variables

Custom suite teardown
    the user closes the browser
    Disconnect from database

the user completes initial details of the competition
    [Arguments]    ${competitionName}   ${fundingType}
    the user navigates to the page               ${CA_UpcomingComp}
    the user clicks the button/link              jQuery = .govuk-button:contains("Create competition")
    the user fills in the CS Initial details     ${competitionName}  ${month}  ${nextyear}  ${compType_Programme}  2   ${fundingType}

the user navigates to completition stage
    the user clicks the button/link     link = Initial details
    the user clicks the button/link     jQuery = span:contains("Completion stage")

the user saves the completion stage with competition close
    [Arguments]     ${completionStage}
    the user selects the radio button     selectedCompletionStage   ${completionStage}
    the user clicks the button/link       jQuery = button:contains("Done")

the user should see competition close in read only page
    [Arguments]     ${completionStageValue}
    the user clicks the button/link     jQuery = span:contains("Completion stage")
    the user should see the element     jQuery = strong:contains("${completionStageValue}")

the user fills in the competition close Milestones
    ${i} =  Set Variable   1
     :FOR   ${ELEMENT}   IN    @{sbriType1Milestones}
      \    the user enters text to a text field     jQuery = th:contains("${ELEMENT}") ~ td.day input  ${i}
      \    the user enters text to a text field     jQuery = th:contains("${ELEMENT}") ~ td.month input  ${month}
      \    the user enters text to a text field     jQuery = th:contains("${ELEMENT}") ~ td.year input  ${nextyear}
      \    ${i} =   Evaluate   ${i} + 1
    the user clicks the button/link     jQuery = button:contains("Done")

the user should see milestones section marked as complete
    the user clicks the button/link     link = Competition details
    the user should see the element     jQuery = div:contains("Milestones") ~ .task-status-complete

the user assign the stakeholder to the SBRI competition
    log in as a different user          &{ifs_admin_user_credentials}
    the user navigates to the page      ${server}/management/competition/setup/${openSBRICompetitionId}/manage-stakeholders
    the user clicks the button/link     jQuery = td:contains("Rayon Kevin") button[type="submit"]

the user should only see application related key statistics
    the user should not see the element     jQuery = small:contains("Assessors invited")
    the user should not see the element     jQuery = small:contains("Invitations accepted")
    the user should not see the element     jQuery = small:contains("Applications per assessor")
    the user should see the element         jQuery = small:contains("Applications started")
    the user should see the element         jQuery = small:contains("Applications beyond 50%")
    the user should see the element         jQuery = small:contains("Applications submitted")

the user should only see application related competition navigation links
    ${STATUS}    ${VALUE} =     Run Keyword And Ignore Error Without Screenshots    the user should see the element    jQuery = a:contains("Applications: All, submitted, ineligible")
    Run Keyword If    '${status}' == 'FAIL'    the user should see the element    jQuery = a:contains("Applications: Submitted, ineligible")
    the user should not see the element     jQuery = a:contains("Invite assessors to assess the competition")
    the user should not see the element     jQuery = a:contains("Manage assessments")
    the user should not see the element     jQuery = a:contains("Manage assessment panel")
    the user should not see the element     jQuery = a:contains("Manage interview panel")
    the user should not see the element     jQuery = a:contains("Input and review funding decision")

the user should only see competition close completion stage milestones
    the user should see the element         jQuery = h3:contains("Open date")
    the user should see the element         jQuery = h3:contains("Briefing event")
    the user should see the element         jQuery = h3:contains("Submission date")
    the user should not see the element     jQuery = h3:contains("Allocate assessors")
    the user should not see the element     jQuery = h3:contains("Assessor briefing")
    the user should not see the element     jQuery = h3:contains("Assessor accepts")
    the user should not see the element     jQuery = h3:contains("Assessor deadline")
    the user should not see the element     jQuery = h3:contains("Line draw")
    the user should not see the element     jQuery = h3:contains("Assessment panel")
    the user should not see the element     jQuery = h3:contains("Panel date")
    the user should not see the element     jQuery = h3:contains("Funders panel")
    the user should not see the element     jQuery = h3:contains("Notifications")
    the user should not see the element     jQuery = h3:contains("Release feedback")

the user should only see application related key statistics in applications page
    the user should not see the element     jQuery = small:contains("Assessors invited")
    the user should see the element         jQuery = small:contains("Applications beyond 50%")
    the user should see the element         jQuery = small:contains("Applications submitted")
    the user should see the element         jQuery = small:contains("Ineligible applications")

the user should see the correct data on finance check page
    the user should see the element       jQuery = dt:contains("Total project cost") ~ dd:contains("£265,084") ~dt:contains("Funding applied for") ~ dd:contains("£77,057") ~ dt:contains("Current amount") ~ dd:contains("£77,057")
    the user should not see the element       jQuery = dt:contains("Other public sector funding")
    the user should not see the element       jQuery = dt:contains("Total percentage grant")
    the user should not see the element       jQuery = a:contains("View"):contains("finances")

the user should see calculations without VAT
    the user should not see the element     jQuery = label:contains("Total project costs inclusive of VAT")
    the user clicks the button/link         link = Finance checks
    the user should see the element         jQuery = dt:contains("Total project cost") ~ dd:contains("£220,903") ~ dt:contains("Funding applied for") ~ dd:contains("£77,057") ~ dt:contains("Current amount") ~ dd:contains("£63,803")
    the user clicks the button/link         css = .eligibility-0

the user should see calculations with VAT
    the user should see the element     jQuery = div:contains("Total project costs inclusive of VAT") ~ div:contains("£265,084")
    the user clicks the button/link     link = Finance checks
    the user should see the element     jQuery = dt:contains("Total project cost") ~ dd:contains("£265,084") ~dt:contains("Funding applied for") ~ dd:contains("£77,057") ~ dt:contains("Current amount") ~ dd:contains("£77,057")
    the user clicks the button/link     css = .eligibility-0

the external user should see the correct VAT information
    the user should see the element     jQuery = legend:contains("Are you VAT registered") ~ span:contains("Yes")
    the user should see the element     jQuery = div:contains("Total VAT") ~ div:contains("£44,181")
    the user should see the element     jQuery = div:contains("Total project costs inclusive of VAT") ~ div:contains("£265,084")