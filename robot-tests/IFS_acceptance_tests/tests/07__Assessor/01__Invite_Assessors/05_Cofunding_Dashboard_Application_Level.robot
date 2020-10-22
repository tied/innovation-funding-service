*** Settings ***
Documentation    IFS-8403  Co funder dashboard - application level
...
...              IFS-8408  Co funder view of application
...
Suite Setup       Custom suite setup
Suite Teardown    the user closes the browser
Resource          ../../../resources/defaultResources.robot
Resource          ../../../resources/common/Applicant_Commons.robot
Resource          ../../../resources/common/Competition_Commons.robot
Resource          ../../../resources/common/PS_Common.robot
Resource          ../../../resources/common/Assessor_Commons.robot

*** Variables ***
${ktpCofundingCompetitionNavigation}      Co funder dashboard - application level
${cofunderUserUsername}                   Wallace.Mccormack@money.com
${cofundingCompetitionName}               KTP cofunding
${cofundingCompetitionID}                 ${competition_ids['${cofundingCompetitionName}']}
${cofundingApplicationTitle}              How cancer invasion takes shape

*** Test Cases ***
Cofunder can see list of applications assigned to him in the dashboard
    [Documentation]  IFS-8403
    Given log in as a different user         ${cofunderUserUsername}   ${short_password}
    When the user navigates to the page      ${server}/assessment/cofunder/dashboard/competition/${cofundingCompetitionID}
    Then the user should see the element     jQuery = h1:contains("Review applications") span:contains("${cofundingCompetitionName}")
    And the user should see the element      link = View competition brief (opens in a new window)

Cofunder checks number of applications in the page is no more than 20
    [Documentation]  IFS-8403
    When the user gets the number of applications in page
    Then should be equal as numbers                           ${applicationCount_1}    20

Cofunder can navgate to the next page of applications in review
    [Documentation]  IFS-8403
    Given the user navigates to the page     ${server}/assessment/cofunder/dashboard/competition/${cofundingCompetitionID}
    When the user clicks the button/link     link = Next
    Then the user should see the element     link = Previous

Cofunder checks the number of applications count is correct
    [Documentation]  IFS-8403
    When the user gets the actual number of applications in all pages
    And the user gets expected number of applications in the page
    Then should be equal as numbers                                       ${actualNumberOfApplications}   ${expectedNumberOfApplications}

Cofunder can view read only view of an application and see the print application link
    [Documentation]  IFS-8408
    Given the user clicks the button/link       link = Previous
    And the user clicks the button/link         link = ${cofundingApplicationTitle}
    When the user clicks the button/link        jQuery = button:contains("Application team")
    Then the user should see the element        jQuery = h1:contains("Application overview") span:contains("${cofundingApplicationTitle}")
    And the user should not see the element     jQuery = button:contains("Edit")
    And the user should see the element         jQuery = a:contains("Print application")

*** Keywords ***
Custom suite setup
    The user logs-in in new browser     &{ifs_admin_user_credentials}

the user gets the number of applications in page
   ${pages} =   get element count      css = [class="pagination-links govuk-body"] a
        :FOR    ${i}    IN RANGE   1   ${pages}+1
            \    the user navigates to the page   ${server}/assessment/cofunder/dashboard/competition/${cofundingCompetitionID}?page=${i}
            \    ${applicationCount} =   get element count    jQuery = h2:contains("Applications for review") ~ ul li
            \    set suite variable    ${applicationCount_${i}}    ${applicationCount}

the user gets the actual number of applications in all pages
    ${actualNumberOfApplications}     evaluate     ${applicationCount_1} + ${applicationCount_2}
    set suite variable     ${actualNumberOfApplications}

the user gets expected number of applications in the page
    ${expectedNumberOfApplications} =  get text     jQuery = h2:contains("Applications for review") span
    set suite variable     ${expectedNumberOfApplications}

