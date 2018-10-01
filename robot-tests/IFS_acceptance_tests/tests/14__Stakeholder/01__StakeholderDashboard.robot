*** Settings ***
Documentation   IFS-4189 Add Remove Stakeholders
...
Resource        ../../resources/defaultResources.robot
Resource        ../02__Competition_Setup/CompAdmin_Commons.robot

*** Test Cases ***
The Stakeholder can see their dashboard
    [Documentation]  IFS-4189
    [Tags]
    When the user logs-in in new browser    &{stakeholder_user}
    Then the user should see the element    jQuery = h1:contains("All competitions")

The Stakeholder can search for a competition
    [Documentation]  IFS-4189
    [Tags]
    Given the user enters text to a text field    searchQuery  ${openGenericCompetition}
    When the user clicks the button/link          id = searchsubmit
    And the user clicks the button/link           link = ${openGenericCompetition}
    Then the user should see the element          jQuery = h1:contains("${openGenericCompetitionId}: ${openGenericCompetition}")

*** Keywords ***
