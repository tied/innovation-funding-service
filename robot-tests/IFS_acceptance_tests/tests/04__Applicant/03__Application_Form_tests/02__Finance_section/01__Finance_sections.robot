*** Settings ***
Documentation     INFUND-45: As an applicant and I am on the application form on an open application, I expect the form to help me fill in financial details, so I can have a clear overview and less chance of making mistakes.
...
...               INFUND-1815: Small text changes to registration journey following user testing
...
...               INFUND-2965: Investigation into why financials return to zero when back spacing
...
...               INFUND-2051: Remove the '0' in finance fields
...
...               INFUND-2961: ‘Working days per year’ in Labour Costs do not default to 232.
...
...               INFUND-7522:  Create 'Your finances' view excluding 'Your organisation' page where 'Organisation type' is 'Research' and sub category is 'Academic'
...
...               INFUND-8355: Project finance team - overheads
...
...               IFS-2879: As a Research applicant I MUST accept the grant terms and conditions
Suite Setup       Custom Suite Setup
Suite Teardown    The user closes the browser
Force Tags        Applicant
Resource          ../../../../resources/defaultResources.robot
Resource          ../../Applicant_Commons.robot

*** Variables ***
${applicationName}  ${OPEN_COMPETITION_APPLICATION_5_NAME}
# ${OPEN_COMPETITION_APPLICATION_2_NAME} == Planetary science Pluto\'s telltale heart

*** Test Cases ***
Finance sub-sections
    [Documentation]    INFUND-192
    [Tags]
    Then the user should see all the Your-Finances Sections

Not requesting funding guidance
    [Documentation]    INFUND-7093
    [Tags]
    Given the user should not see the funding guidance
    When the user clicks the button/link                jQuery = summary span:contains("Not requesting funding")
    Then the user should see the funding guidance
    When the user clicks the button/link                jQuery = summary span:contains("Not requesting funding")
    Then the user should not see the funding guidance

Not requesting funding button
    [Documentation]    INFUND-7093
    [Tags]
    When the user clicks the button/link                jQuery = summary span:contains("Not requesting funding")
    And the user clicks the button/link                 jQuery = button:contains("Not requesting funding")
    Then the user should see the funding guidance
    And the user should see the element                 jQuery = button:contains("Requesting funding")
    And the user should see the element                 jQuery = li:nth-of-type(3) span:contains("No action required")
    And the user should see the element                 jQuery = li:nth-of-type(4) span:contains("No action required")

Requesting funding button
    [Documentation]    INFUND-7093
    [Tags]
    When the user clicks the button/link                jQuery = button:contains("Requesting funding")
    Then the user should see the element                jQuery = li:nth-of-type(2) > .task-status-incomplete
    And the user should not see the element             jQuery = li:nth-of-type(3) span:contains("No action required")
    And the user should not see the element             jQuery = li:nth-of-type(3) > .task-status-complete
    And the user should not see the funding guidance

Organisation name visible in the Finance section
    [Documentation]    INFUND-1815
    [Tags]
    When the user clicks the button/link    link = Your project costs
    Then the user should see the element    jQuery = h2:contains("Provide the project costs for 'org2'")          #text in the page    Provide the project costs for '${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}'
    And the user should see the element     jQuery = label:contains("'org2' Total project costs")          #text in the page     '${FUNDERS_PANEL_APPLICATION_1_LEAD_ORGANISATION_NAME}' Total project costs

Guidance in the your project costs
    [Documentation]    INFUND-192
    [Tags]
    [Setup]  Applicant navigates to the finances of the Robot application
    Given the user clicks the button/link   link = Your project costs
    When the user clicks the button/link    jQuery = button:contains("Labour")
    And the user clicks the button/link     css = .govuk-details summary
    Then the user should see the element    css = .govuk-details__text p
    And the user should see the element     css = .labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input[value=""]

Working days per year should be 232
    [Documentation]    INFUND-2961
    Then the working days per year should be 232 by default

User pressing back button should get the correct version of the page
    [Documentation]    INFUND-2695
    [Tags]
    [Setup]  Applicant navigates to the finances of the Robot application
    And the user clicks the button/link     link = Your project costs
    Given The user adds three material rows
    capture page screenshot
    When the user navigates to another page
    And the user should be redirected to the correct page without the usual headers    ${project_guidance}
    And the user goes back to the previous page
    Then the user should see the element    css = table[id=material-costs-table] tbody tr:nth-of-type(3) td:nth-of-type(2) input
    [Teardown]    the user removes the materials rows

Non-academic partner finance section
    [Documentation]    INFUND-7522
    [Tags]
    [Setup]  Log in as a different user     &{collaborator1_credentials}
    Given the user navigates to Your-finances page  ${applicationName}
    And The user should see the element     jQuery = .govuk-details__summary:contains("Not requesting funding")
    And the user should see the element     link = Your project costs
    And the user should see the element     link = Your organisation
    When the user clicks the button/link    link = Your funding
    Then the user should see the element    jQuery = .govuk-list li:contains("the lead applicant must mark the research category page as complete")

Academic partner finance section
    [Documentation]    INFUND-7522
    [Tags]
    [Setup]  Log in as a different user             &{collaborator2_credentials}
    Given the user navigates to Your-finances page  ${applicationName}
    Then The user should not see the element        link = Not requesting funding
    And the user should see the element             link = Your project costs
    And the user should not see the element         link = Your organisation
    And the user should not see the element         link = Your funding
    And the user should not see the element         link = application details

Academic partner can upload file for field J-es PDF
    [Documentation]    INFUND-7522
    [Tags]
    Given the user navigates to Your-finances page  ${applicationName}
    And the user clicks the button/link             link = Your project costs
    # Note the Jes form is already uploaded
    Then the user should see the element            css = p.uploaded-file a
    When The user clicks the button/link            jQuery = button:contains("Remove")
    And the user should see the element             jQuery = label.button-secondary
    And the user uploads the file                   css = .upload-section input  ${5mb_pdf}
    And the user should see the text in the page    ${5mb_pdf}

Compadmin can open the jes-file in applications
    [Documentation]     IFS-102
    [Tags]
    [Setup]  log in as a different user    &{Comp_admin1_credentials}
    Given the user navigates to the page   ${openCompetitionManagementRTO}
    And the user clicks the button/link    link = Applications: All, submitted, ineligible
    And the user clicks the button/link    link = All applications
    And the user clicks the button/link    link = ${OPEN_COMPETITION_APPLICATION_5_NUMBER}
    Then the user clicks the button/link   jQuery = button:contains("Finances summary")
    And the user should not see an error in the page
    And the user navigates to the page     ${openCompetitionManagementRTO}

File upload mandatory for Academic partner to mark section as complete
    [Documentation]    INFUND-8469  IFS-2879
    [Tags]
    [Setup]  Log in as a different user               &{collaborator2_credentials}
    # This will also check the auto-save as we haven't marked finances as complete yet
    Given the user navigates to Your-finances page    ${applicationName}
    And the user clicks the button/link               link = Your project costs
    And the element should be disabled                id = mark-all-as-complete
    And the user clicks the button/link               jQuery = button:contains("Remove")
    When the user selects the checkbox                termsAgreed
    And the user clicks the button/link               jQuery = button:contains("Mark as complete")
    Then the user should see a field error            You must upload a Je-S file

Applicant chooses Calculate overheads option
    [Documentation]     INFUND-6788  INFUND-8191  INFUND-7405  INFUND-8355
    [Tags]
    [Setup]  log in as a different user                     &{lead_applicant_credentials}
    # This test also checks read only view of the overheads once section is marked as complete
    When the user navigates to Your-finances page           ${applicationName}
    Then the user fills in the project costs                Calculate  185,997
    And wait until element is not visible without screenshots  css = .task-list li:nth-of-type(1) .task-status-incomplete
    When the user clicks the button/link                    link = Your project costs
    Then the user should see the text in the page           ${excel_file}
    And the user clicks the button/link                     jQuery = button:contains("Edit your project costs")
    And the user clicks the button/link                     css = button[name="overheadfiledelete"]
    When the user selects the checkbox                      stateAidAgreed
    And the user clicks the button/link                     jQuery = button:contains("Mark as complete")
    Then the user should see a summary error                You cannot mark as complete.

*** Keywords ***
Custom Suite Setup
    log in and create new application if there is not one already  Robot test application
    Applicant navigates to the finances of the Robot application

the user adds three material rows
    the user expands the section          Materials
    the user enters text to a text field  css = table[id=material-costs-table] tbody tr:nth-of-type(1) td:nth-of-type(2) input  01
    ${pagination} =   Run Keyword And Ignore Error Without Screenshots  wait until element is visible  css = table[id=material-costs-table] tr:nth-of-type(2)
    run keyword if    ${pagination} == 'PASS'  click element  jQuery = table[id=material-costs-table] tr:nth-of-type(2) .button-clear:contains("Remove")
    the user clicks the button/link       jQuery = button:contains("Add another materials cost")
    the user enters text to a text field  css = table[id=material-costs-table] tbody tr:nth-of-type(2) td:nth-of-type(2) input  01
    the user clicks the button/link       jQuery = button:contains("Add another materials cost")
    the user enters text to a text field  css = table[id=material-costs-table] tbody tr:nth-of-type(3) td:nth-of-type(2) input  01
    the user moves focus to the element   link = Please refer to our guide to project costs for further information.
    Wait for autosave

the user removes the materials rows
    [Documentation]    INFUND-2965
    the user clicks the button/link                          jQuery = table[id=material-costs-table] button:contains("Remove")
    Wait Until Element Is Not Visible Without Screenshots    css = table[id=material-costs-table] tbody tr:nth-of-type(4) td:nth-of-type(2) input    10s
    the user moves focus to the element                      jQuery = table[id=material-costs-table] button:contains("Remove")
    the user clicks the button/link                          jQuery = table[id=material-costs-table] button:contains("Remove")
    Wait Until Element Is Not Visible Without Screenshots    css = table[id=material-costs-table] tbody tr:nth-of-type(3) td:nth-of-type(2) input    10s
    the user clicks the button/link                          jQuery = table[id=material-costs-table] button:contains("Remove")
    Run Keyword And Ignore Error Without Screenshots         the user clicks the button/link    jQuery = table[id=material-costs-table] button:contains("Remove")
    Wait Until Element Is Not Visible Without Screenshots    css = table[id=material-costs-table] tbody tr:nth-of-type(2) td:nth-of-type(2) input    10s
    the user clicks the button/link                          jQuery = button:contains("Materials")

the working days per year should be 232 by default
    the user should see the element    css = [name^="labour-labourDaysYearly"]
    ${Days_value} =   Get Value        css = [name^="labour-labourDaysYearly"]
    Should Be Equal As Strings         ${Days_value}    232

the user navigates to another page
    the user clicks the button/link    link = Please refer to our guide to project costs for further information.
    Run Keyword And Ignore Error Without Screenshots    Confirm Action

the user should see the funding guidance
    [Documentation]    INFUND-7093
    the user should see the element    css = .govuk-details__text p

the user should not see the funding guidance
    [Documentation]    INFUND-7093
    the user should not see the element    css = .govuk-details__text p