*** Settings ***
Documentation     IFS-786 Assessment panels - Manage assessment panel link on competition dashboard
...
...               IFS-31 Assessment panels - Invite assessors to panel- Find and Invite Tabs
...
...               IFS-1560 Assessment panels - Invite assessors to panel - Invite assessors
...
...               IFS-1564 Assessment panels - Invite assessors to panel - Key statistics
...
...               IFS-1561 Assessment panels - Invite assessors to panel - Overview tab and resend invites
...
...               IFS-37 Assessment panels - Accept/Reject Panel Invite
...
...               IFS-1563 Assessment panels - Invite assessors to panel - Accepted tab

Suite Setup       Custom Suite Setup
Suite Teardown    The user closes the browser
Force Tags        CompAdmin
Resource          ../../resources/defaultResources.robot

*** Variables ***
${assessment_panel}  ${server}/management/assessment/panel/competition/${CLOSED_COMPETITION}
${panel_user_joel}  joel.george@gmail.com

*** Test Cases ***
Assement panel link is deactivated if the assessment panel is not set
    [Documentation]  IFS-786
    [Tags]  HappyPath
    Given The user clicks the button/link  link=${CLOSED_COMPETITION_NAME}
    Then the user should see the element   jQuery=.disabled:contains("Manage assessment panel")

Assessment panel links are active if the assessment panel has been set
    [Documentation]  IFS-786
    [Tags]  HappyPath
    [Setup]  enable assessment panel for the competition
    Given the user clicks the button/link  link=Manage assessment panel
    When the user clicks the button/link   link=Invite assessors to attend
    Then the user should see the element   jQuery=h1:contains("Invite assessors to panel")

There are no Assessors in Invite and Overview tab before sending invite
    [Documentation]  IFS-1561
    [Tags]
    Given the user clicks the button/link  link=Invite
    And the user should see the element    jQuery=tr:contains("There are no assessors to be invited to this competition.")
    Then the user clicks the button/link   link=Overview
    And the user should see the element    jQuery=tr:contains("There are no assessors invited to this assessment panel.")

CompAdmin can add an assessor to invite list
    [Documentation]  IFS-31
    [Tags]  HappyPath
    [Setup]  the user clicks the button/link  link=Find
    Given the user clicks the button/link    jQuery=tr:contains("Benjamin Nixon") label
    And the user clicks the button/link      jQuery=tr:contains("Joel George") label
    When the user clicks the button/link     jQuery=button:contains("Add selected to invite list")
    Then the user should see the element     jQuery=td:contains("Benjamin Nixon") + td:contains("benjamin.nixon@gmail.com")
    And the user should see the element      jQuery=td:contains("Joel George") + td:contains("${panel_user_joel}")
    And the user clicks the button/link      link=Find
    And the user should not see the element  jQuery=td:contains("Benjamin Nixon")
    And the user should not see the element  jQuery=td:contains("Joel George")

Cancel sending invite returns to the invite tab
    [Documentation]  IFS-1560
    [Tags]
    [Setup]  the user clicks the button/link  link=Invite
    Given the user clicks the button/link     link=Review and send invites
    And the user should see the element       jQuery=h2:contains("Recipients") ~ p:contains("Benjamin Nixon")
    When the user clicks the button/link      link=Cancel
    Then the user should see the element      jQuery=td:contains("Benjamin Nixon")

Assessor recieves the invite to panel
    [Documentation]  IFS-1560  IFS-1564
    [Tags]  HappyPath
    [Setup]  the user clicks the button/link  link=Invite
    Given the user clicks the button/link     link=Review and send invites
    When the user clicks the button/link      jQuery=button:contains("Send invite")
    And the user reads his email              benjamin.nixon@gmail.com  Invitation to assess '${CLOSED_COMPETITION_NAME}'  We are inviting you to the assessment panel
    And the user reads his email              joel.george@gmail.com  Invitation to assess '${CLOSED_COMPETITION_NAME}'  We are inviting you to the assessment panel
    And the user should see the element      jQuery=.column-quarter:contains("2") small:contains("Invited")
    And the user should see the element      jQuery=.column-quarter:contains("2") small:contains("Assessors on invite list")

Bulk add assessor to invite list
    [Documentation]  IFS-31
    [Tags]
    [Setup]  the user clicks the button/link   link=Find
    Given the user selects the checkbox     select-all-check
    And the user clicks the button/link     jQuery=button:contains("Add selected to invite list")
    And the user should see the element     jQuery=td:contains("Madeleine Martin") + td:contains("madeleine.martin@gmail.com")
    When the user clicks the button/link    link=Find
    Then the user should see the element    jQuery=td:contains("No available assessors found")

CompAdmin resend invites to multiple assessors
    [Documentation]  IFS-1561
    [Tags]  HappyPath
    [Setup]  the user clicks the button/link    link=Overview
    Given the user clicks the button/link         jQuery=tr:contains("Benjamin Nixon") label
    And the user clicks the button/link         jQuery=tr:contains("Joel George") label
    And the user clicks the button/link         jQuery=button:contains("Resend invites")
    And the user should see the element         jQuery=h2:contains("Recipients") ~ p:contains("Benjamin Nixon")
    When the user clicks the button/link        jQuery=button:contains("Send invite")
    Then the user should see the element        jQuery=td:contains("Benjamin Nixon") ~ td:contains("Invite sent: ${today}")
    And the user should see the element         jQuery=td:contains("Joel George") ~ td:contains("Invite sent: ${today}")

Assesor is able to accept the invitation
    [Documentation]  IFS-37
    [Tags]  HappyPath
    [Setup]  Log in as a different user     benjamin.nixon@gmail.com  ${short_password}
    Given the user reads his email and clicks the link  benjamin.nixon@gmail.com  Invitation to assess '${CLOSED_COMPETITION_NAME}'  We are inviting you to the assessment panel  1
    When the user selects the radio button  acceptInvitation  true
    And The user clicks the button/link     jQuery=button:contains("Confirm")
    Then the user should see the element    jQuery=h1:contains("Assessor dashboard")  #TODO to be updated once ifs-1135 goes in

Assesor is able to reject the invitation
    [Documentation]  IFS-37
    [Tags]
    [Setup]  Logout as user
    Given the user reads his email and clicks the link  joel.george@gmail.com  Invitation to assess '${CLOSED_COMPETITION_NAME}'  We are inviting you to the assessment panel
    When the user selects the radio button  acceptInvitation  false
    And The user clicks the button/link     jQuery=button:contains("Confirm")
    And the user clicks the button/link     link=Sign in
    And Logging in and Error Checking   ${panel_user_joel}  ${short_password}
    Then the user should see the element    jQuery=h1:contains("Assessor dashboard")  #TODO to be updated once ifs-1135 goes in

Comp Admin can see the rejected and accepted invitation
    [Documentation]  IFS-37 IFS-1563
    [Tags]
    [Setup]  Log in as a different user        &{Comp_admin1_credentials}
    Given the user navigates to the page       ${SERVER}/management/assessment/panel/competition/${CLOSED_COMPETITION}/assessors/overview
    And the user should see the element        jQuery=td:contains("Joel George") ~ td:contains("Invite declined")
    When the user clicks the button/link        link=Accepted
    Then the user should see the element        jQuery=td:contains("Benjamin Nixon") ~ td:contains("Materials, process and manufacturing design technologies")
    When the user clicks the button/link       link=Overview
    Then the user should not see the element   jQuery=td:contains("Benjamin Nixon")

*** Keywords ***

Custom Suite Setup
    The user logs-in in new browser  &{Comp_admin1_credentials}
    ${today} =  get today short month
    set suite variable  ${today}

enable assessment panel for the competition
    the user clicks the button/link  link=View and update competition setup
    the user clicks the button/link  link=Assessors
    the user clicks the button/link  jQuery=button:contains("Edit")
    the user selects the radio button  hasAssessmentPanel  hasAssessmentPanel-0
    the user clicks the button/link  jQuery=button:contains("Done")
    the user clicks the button/link  link=Competition setup
    the user clicks the button/link  link=All competitions
    the user clicks the button/link  link=${CLOSED_COMPETITION_NAME}
