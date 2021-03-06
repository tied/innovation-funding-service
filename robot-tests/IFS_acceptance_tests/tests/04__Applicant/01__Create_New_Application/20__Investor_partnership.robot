*** Settings ***
Documentation     IFS-7213  Investor Partnerships Funding type & T&Cs
...
...               IFS-7235  Change to terms and conditions labelling for Investor Partnerships
...
...               IFS-8779 Subsidy Control - Create a New Competition - Initial Details
...
...               IFS-9214 Add dual T&Cs to Subsidy Control Competitions
...
Suite Setup       Custom Suite Setup
Suite Teardown    Custom suite teardown
Resource          ../../../resources/defaultResources.robot
Resource          ../../../resources/common/Competition_Commons.robot
Resource          ../../../resources/common/Assessor_Commons.robot

*** Variables ***
${investorCompName}         Investor
${investorCompId}           ${competition_ids["${investorCompName}"]}
${investorApplication}      Investor partnership
${investorApplicationId}    ${application_ids["${investorApplication}"]}
${tandcLink}                Investor Partnerships terms and conditions
${investorPSApplication}    Investor ps application
${investorPSApplicationId}  ${application_ids["${investorPSApplication}"]}
${investorApplicationLink}  ${server}/management/competition/${investorCompId}/application/${investorApplicationId}
${investorFeedbackLink}     ${server}/application/${investorPSApplicationId}/summary


*** Test Cases ***
Investor partnership initial details
    [Documentation]  IFS-7213
    Given the user fills in initial details
    When the user clicks the button/link      link = Initial details
    Then the user should see the element      jQuery = dt:contains("Funding type") ~ dd:contains("Investor Partnership")

Edit view of initial details
    [Documentation]  IFS-7213
    Given the user clicks the button/link     css = button[type="submit"]
    Then the user should see the element      jQuery = dt:contains("Funding type") ~ dd:contains("Investor Partnership")
    [Teardown]   navigate to comp setup of investor comp

Creating a new investor comp points to the correct T&C
    [Documentation]  IFS-7213
    Given the user fills in initial details
    When the user clicks the button/link                     link = Terms and conditions
    Then the user sees that the radio button is selected     termsAndConditionsId  31

The Investor partnership t&c's are correct
    [Documentation]  IFS-7213
    When the user clicks the button/link     link = Investor Partnerships (opens in a new window)
    And select window                        title = Award terms and conditions - Innovation Funding Service
    Then the user should see the element     jQuery = h1:contains("Terms and conditions of an Innovate UK investor partnerships competition")
    And close window
    And select window                        title = Competition terms and conditions - Innovation Funding Service

T&c's can be confirmed
    [Documentation]  IFS-7213  IFS-9214
    Given the user clicks the button/link      jQuery = button:contains("Done")
    When the user selects the radio button     termsAndConditionsId  31
    And the user clicks the button/link        jQuery = button:contains("Done")
    And the user should see the element        jQuery = dt:contains("Subsidy control terms and conditions") ~ dd:contains("Investor Partnerships")
    And the user should see the element        jQuery = dt:contains("State aid terms and conditions") ~ dd:contains("Investor Partnerships")
    Then the user clicks the button/link       link = Back to competition details
    And the user should see the element        jQuery = li:contains("Terms and conditions") .task-status-complete

Applicant is able to see correct T&C's
    [Documentation]  IFS-7213
    Given Log in as a different user         ${peter_styles_email}  ${short_password}
    when the user clicks the button/link     link = Investor partnership
    And the user clicks the button/link      link = ${tandcLink}
    Then the user should see the element     jQuery = h2:contains("Terms and conditions of an Innovate UK investor partnerships competition")

Applicant can confirm t&c's
    [Documentation]  IFS-7235
    Given the user selects the checkbox      agreed
    When the user clicks the button/link     css = button[type="submit"]
    And the user clicks the button/link      link = Back to application overview
    Then the user should see the element     jQuery = li:contains("${tandcLink}") .task-status-complete

Internal user sees correct label for T&C's
    [Documentation]  IFS-7235
    Given Log in as a different user         &{Comp_admin1_credentials}
    When the user navigates to the page      ${investorApplicationLink}
    Then the user should see the element     jQuery = button:contains("${tandcLink}")

Application feedback page shows the correct link for t&c's
    [Documentation]  IFS-7235  IFS-8066
    Given Log in as a different user         &{troy_ward_crendentials}
    When the user navigates to the page      ${investorFeedbackLink}
    And the user clicks the button/link      jQuery = button:contains("Investor Partnerships terms and conditions")
    Then the user should see the element     jQuery = a:contains("Investor Partnerships")

*** Keywords ***
Custom suite setup
    Set predefined date variables
    The user logs-in in new browser   &{Comp_admin1_credentials}

Custom Suite teardown
    Close browser and delete emails

the user fills in initial details
    the user navigates to the page               ${CA_UpcomingComp}
    the user clicks the button/link              jQuery = .govuk-button:contains("Create competition")
    the user fills in the CS Initial details     Investor comp  ${month}  ${nextyear}  ${compType_Programme}  SUBSIDY_CONTROL  INVESTOR_PARTNERSHIPS

navigate to comp setup of investor comp
    the user clicks the button/link             jQuery = button:contains("Done")
    the user clicks the button/link             link = Back to competition details
