*** Settings ***
Documentation     INFUND-5629 As a Competitions team member I want to be able to edit Application Questions individually in Competition Setup so that I can manage the question and associated applicant and assessor guidance in one place
...
...               INFUND-6468 Competition setup autosave should be validating types, allowing invalid data and doing a complete validate on mark as complete
Suite Setup       Custom suite setup
Force Tags        CompAdmin
Resource          ../../resources/defaultResources.robot
Resource          CompAdmin_Commons.robot

*** Test Cases ***
Business opportunity Server-side validations setup questions
    [Documentation]    INFUND-5629 INFUND-5685
    [Tags]    HappyPath
    Given The user clicks the button/link  link=Application
    And The user clicks the button/link    jQuery=a:contains("Business opportunity")
    When the user leaves all the question field empty
    And The user clicks the button/link                             css=button[type="submit"]
    Then the validation error above the question should be visible  jQuery=label:contains(Question title)  This field cannot be left blank.
    And the validation error above the question should be visible   jQuery=label:contains(Question guidance title)  This field cannot be left blank.
    And the validation error above the question should be visible   jQuery=label:contains(Question guidance)  This field cannot be left blank.
    And the validation error above the question should be visible   jQuery=label:contains(Max word count)  This field cannot be left blank.
    And the user should see a summary error                         This field cannot be left blank.

Business opportunity Sever-side validations assessment questions
    [Documentation]    INFUND-5685
    [Tags]    HappyPath
    [Setup]
    Given the user leaves all the assessment questions empty
    When the user clicks the button/link    css=button[type="submit"]
    Then the user should see a field error   Please enter a from score.
    And the user should see a field error    Please enter a to score.
    And the user should see a field error    Please enter a justification.
#   TODO commented due to IFS-2621
#    And the user should see a summary error  Please enter a from score.
#    And the user should see a summary error  Please enter a to score.
#    And the user should see a summary error  Please enter a justification.

Business opportunity: Client side validations
    [Documentation]    INFUND-5629 INFUND-5685
    [Tags]    HappyPath
    Given the user fills the empty question fields
    And the user enters text to a text field    id=question.shortTitle    Test Heading
    And the user moves focus and waits for autosave
    And the user fills the empty assessment fields
    Then the validation error above the question should not be visible  jQuery=label:contains(Question title)    This field cannot be left blank.
    And the validation error above the question should not be visible   jQuery=label:contains(Question guidance title)    This field cannot be left blank.
    And the validation error above the question should not be visible   jQuery=label:contains(Question guidance)    This field cannot be left blank.
    And the validation error above the question should not be visible   jQuery=label:contains(Max word count)    This field cannot be left blank.
    And the user should not see the text in the page    Please enter a from score.
    And the user should not see the text in the page    Please enter a to score.
    And the user should not see the text in the page    Please enter a justification.

Business opportunity: Autosave
    [Documentation]    INFUND-5629 INFUND-5685
    [Tags]  HappyPath
    Given the user moves focus and waits for autosave
    When the user clicks the button/link    link=Application
    And The user clicks the button/link    jQuery=a:contains("Test Heading")
    Then the user should see the correct inputs in the Applications questions form
    And the user should see the correct inputs in assessment questions

Test Heading: Mark as done
    [Documentation]    INFUND-5629
    [Tags]    HappyPath
    When The user clicks the button/link         css=button[type="submit"]
    And the user clicks the button/link          jQuery=a:contains("Test Heading")
    Then the user should see the element         jQuery=h1:contains("Test Heading")
    And the user should see the element          jQuery=dt:contains("Question title") + dd:contains("Test title")
    And the user should see the element          jQuery=dt:contains("Max word count") + dd:contains("150")
    [Teardown]  the user clicks the button/link  link=Application

Scope: Sever-side validations assessment questions
    [Documentation]    INFUND-6444
    [Tags]
    Given the user clicks the button/link               link=Scope
    When the user clicks the button/link                jQuery=Button:contains("+Add guidance row")
    And the user clicks the button/link                 css=button[type="submit"]
    Then the user should see a field and summary error  Please enter a value.
    And the user should see a field and summary error   Please enter a justification
    And The user clicks the button/link                 id=remove-guidance-row-2
    And the user clicks the button/link                 css=button[type="submit"]
    And the user should not see the element             jQuery=a:contains("Please enter a value")
    And the user should not see the element             jQuery=a:contains("Please enter a justification")

*** Keywords ***
Custom Suite setup
    The user logs-in in new browser  &{Comp_admin1_credentials}
    ${nextYear} =  get next year
    Set suite variable  ${nextYear}
    User creates a new competition for Application tests

the user leaves all the question field empty
    Clear Element Text    css=.editor
    Press Key    css=.editor    \\8
    focus    css=button[type="submit"]
    wait for autosave
    The user enters text to a text field    id=question.shortTitle    ${EMPTY}
    the user moves focus and waits for autosave
    The user enters text to a text field    id=question.title    ${EMPTY}
    the user moves focus and waits for autosave
    The user enters text to a text field    id=question.guidanceTitle    ${EMPTY}
    the user moves focus and waits for autosave
    The user enters text to a text field    id=question.maxWords    ${EMPTY}
    the user moves focus and waits for autosave

The user leaves all the assessment questions empty
    The user enters text to a text field    id=guidanceRows[0].scoreFrom    ${EMPTY}
    the user moves focus and waits for autosave
    The user enters text to a text field    id=guidanceRows[0].scoreTo    ${EMPTY}
    the user moves focus and waits for autosave
    the user enters text to a text field    id=guidanceRows[0].justification    ${EMPTY}
    the user moves focus and waits for autosave

the validation error above the question should be visible
    [Arguments]    ${QUESTION}    ${ERROR}
    Element Should Contain    ${QUESTION}    ${ERROR}

the validation error above the question should not be visible
    [Arguments]    ${QUESTION}    ${ERROR}
    focus    css=button[type="submit"]
    Wait Until Element Is Not Visible Without Screenshots    css=error-message
    Element Should not Contain    ${QUESTION}    ${ERROR}

the user moves focus and waits for autosave
    focus    link=Sign out
    Wait For Autosave

the user should see the correct inputs in the Applications questions form
    ${input_value} =    Get Value    id=question.shortTitle
    Should Be Equal    ${input_value}    Test Heading
    ${input_value} =    Get Value    id=question.title
    Should Be Equal    ${input_value}    Test title
    ${input_value} =    Get Value    id=question.subTitle
    Should Be Equal    ${input_value}    Subtitle test
    ${input_value} =    Get Value    id=question.guidanceTitle
    Should Be Equal    ${input_value}    Test guidance title
    ${input_value} =    Get Value    id=question.guidance
    Should Be Equal    ${input_value}    Guidance text test
    ${input_value} =    Get Value    id=question.maxWords
    Should Be Equal    ${input_value}    150

The user should see the correct inputs in assessment questions
    ${input_value} =    Get Value    id=guidanceRows[0].scoreFrom
    Should Be Equal    ${input_value}    30
    ${input_value} =    Get Value    id=guidanceRows[0].scoreTo
    Should Be Equal    ${input_value}    35
    ${input_value} =    Get Value    id=guidanceRows[0].justification
    Should Be Equal    ${input_value}    This is a justification

User creates a new competition for Application tests
    Given the user navigates to the page    ${CA_UpcomingComp}
    When the user clicks the button/link    jQuery=.button:contains("Create competition")
    And the user clicks the button/link     link=Initial details
    And the user enters text to a text field    id=title    Test competition
    And the user selects the option from the drop-down menu    Programme    id=competitionTypeId
    And the user selects the option from the drop-down menu    Health and life sciences    id=innovationSectorCategoryId
    And the user selects the option from the drop-down menu    Advanced therapies    css=[id="innovationAreaCategoryIds[0]"]
    And the user enters text to a text field    id=openingDateDay    01
    And the user enters text to a text field    Id=openingDateMonth    12
    And the user enters text to a text field    id=openingDateYear  ${nextYear}
    And the user selects the option from the drop-down menu    Ian Cooper    id=innovationLeadUserId
    And the user selects the option from the drop-down menu    John Doe    id=executiveUserId
    And the user clicks the button/link    jQuery=button:contains("Done")
    And the user clicks the button/link    link=Competition setup