*** Settings ***
Documentation   IFS-5700 - Create new project team page to manage roles in project setup
...
...             IFS-5719 - Add team members in Project setup
...
...             IFS-5718 - Remove team members in Project setup
...
...             IFS-5723 - Remove a pending invitation
...
...             IFS-5722 - Resend invitation to add new members (partners)
...
...             IFS-5720 - Add team members (internal)
...
...             IFS-5721 - Resend invitation to add new members (internal)
...
...             IFS-5721 - Remove a pending invitation (internal)
...
Suite Setup       Custom suite setup
Suite Teardown    Custom suite teardown
Resource          PS_Common.robot

*** Variables ***
${newProjecTeamPage}       ${server}/project-setup/project/${PS_PD_Project_Id}/team
${leadNewMemberEmail}      test@test.nom
${nonLeadNewMemberEmail}   testerina@test.nom
${removeInviteEmail}       remove@test.nom
${internalViewTeamPage}    ${server}/project-setup-management/competition/${PROJECT_SETUP_COMPETITION}/project/${PS_PD_Project_Id}/team
${internalInviteeEmail}    internal@invitee.com

*** Test Cases ***
The lead partner is able to access project team page
    [Documentation]  IFS-5700
    Given the user logs-in in new browser    &{lead_applicant_credentials}
    When the user navigates to the page      ${newProjecTeamPage}
    Then the user should see the element     jQuery = h1:contains("Project team")

Verify add new team member field validation
    [Documentation]  IFS-5719
    Given the user clicks the button/link               jQuery = button:contains("Add team member")
    When the user clicks the button/link                jQuery = button:contains("Invite to project")
    Then the user should see a field and summary error  Please enter a name.
    And the user should see a field and summary error   Enter an email address in the correct format, like name@example.com
    [Teardown]  the user clicks the button/link         jQuery = td:contains("Name")~ td button:contains("Remove")

The lead partner is able to add a new team member
    [Documentation]  IFS-5719
    Given the user adds a new team member   Tester   ${leadNewMemberEmail}
    Then the user should see the element    jQuery = td:contains("Tester (Pending)")
    [Teardown]   Logout as user

A new team member is able to accept the inviation from lead partner and see projec set up
    [Documentation]  IFS-5719
    Given the user reads his email and clicks the link   ${leadNewMemberEmail}  New designs for a circular economy: Magic material: Invitation for project 112.  You have been invited to join the project Magic material by Empire Ltd.  1
    When the user creates a new account                  Tester   Testington   ${leadNewMemberEmail}
    Then the user is able to access the project          ${leadNewMemberEmail}

Non Lead partner is able to add a new team member
    [Documentation]  IFS-5719
    [Setup]  log in as a different user    &{collaborator1_credentials}
    Given the user navigates to the page   ${newProjecTeamPage}
    When the user adds a new team member   Testerina   ${nonLeadNewMemberEmail}
    Then the user should see the element   jQuery = td:contains("Testerina (Pending)")
    [Teardown]   the user logs out if they are logged in

A new team member is able to accept the inviation from non lead partner and see projec set up
    [Documentation]  IFS-5719
    Given the user reads his email and clicks the link      ${nonLeadNewMemberEmail}  New designs for a circular economy: Magic material: Invitation for project 112.  You have been invited to join the project Magic material by Ludlow.  1
    When the user creates a new account                     Testerina   Testington   ${nonLeadNewMemberEmail}
    Then the user is able to access the project             ${nonLeadNewMemberEmail}

A user is able to remove a team member
    [Documentation]  IFS-5718
    [Setup]  log in as a different user        &{collaborator1_credentials}
    Given the user navigates to the page       ${newProjecTeamPage}
    When the user clicks the button/link       jQuery = td:contains("Testerina Testington")~ td a:contains("Remove")
    And the user clicks the button/link        jQuery = td:contains("Testerina Testington")~ td button:contains("Remove user")
    Then the user should not see the element   jQuery = td:contains("Testerina Testington")~ td:contains("Remove")

A user who has been removed is no longer able to access the project
    [Documentation]  IFS-5718
    Given log in as a different user           ${nonLeadNewMemberEmail}   ${short_password}
    Then the user should not see the element   jQuery = li:contains("Project number") h3:contains("Magic material")

A user is able to re-send an invitation
    [Documentation]  IFS-5723
    [Setup]    the user logs-in in new browser              &{lead_applicant_credentials}
    Given the user navigates to the page                    ${newProjecTeamPage}
    When the user adds a new team member                    Removed   ${removeInviteEmail}
    Then the user is able to re-send an invitation
    And the user reads his email                            ${removeInviteEmail}  New designs for a circular economy: Magic material: Invitation for project 112.  You have been invited to join the project Magic material by Empire Ltd.

A partner is able to remove a pending invitation
    [Documentation]  IFS-5723
    Given the user is abe to remove the pending invitation
    Then Removed invitee is not able to accept the invite    ${removeInviteEmail}

An internal user is able to access the project team page
    [Documentation]  IFS-5720
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page   ${internalViewTeamPage}
    Then the user should see the element   jQuery = h1:contains("Project team")

*** Keywords ***
The user is able to re-send an invitation
    the user should see the element   jQuery = td:contains("Removed (Pending)")~ td button:contains("Resend invite")
    the user clicks the button/link   jQuery = td:contains("Removed (Pending)")~ td button:contains("Resend invite")

Removed invitee is not able to accept the invite
    [Arguments]    ${email}
    the user reads his email and clicks the link   ${email}  New designs for a circular economy: Magic material: Invitation for project 112.  You have been invited to join the project Magic material by Empire Ltd.  1
    the user should see the element                jQuery = h1:contains("Sorry, you are unable to accept this invitation.")

The user is abe to remove the pending invitation
    the user clicks the button/link       jQuery = td:contains("Removed (Pending)")~ td button:contains("Remove")
    the user should not see the element   jQuery = td:contains("Removed (Pending)")~ td button:contains("Remove")

The user is able to access the project
    [Arguments]  ${email}
    the user logs-in in new browser   ${email}   ${short_password}
    the user should see the element   link = ${PS_PD_Application_Title}

The user creates a new account
    [Arguments]   ${firstName}   ${lastName}   ${email}
    the user should see the element     jQuery = h1:contains("Join a project")
    the user clicks the button/link     jQuery = a:contains("Create account")
    the user fills in account details   ${firstName}   ${lastName}
    the user clicks the button/link     jQuery = button:contains("Create account")
    the user verifies their account     ${email}

The user verifies their account
    [Arguments]  ${email}
    the user should see the element                jQuery = h1:contains("Please verify your email address")
    the user reads his email and clicks the link   ${email}  Please verify your email address  You have recently set up an account with the Innovation Funding Service.  1
    the user should see the element                jQuery = h1:contains("Account verified")

The user fills in account details
    [Arguments]  ${firstName}  ${lastName}
    the user enters text to a text field   id = firstName     ${firstName}
    the user enters text to a text field   id = lastName      ${lastName}
    the user enters text to a text field   id = phoneNumber   07123456789
    the user enters text to a text field   id = password      ${short_password}
    the user selects the checkbox          termsAndConditions

Custom suite setup
    The guest user opens the browser

Custom suite teardown
   The user closes the browser

