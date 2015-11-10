//Dom based routing 
//------------------
//Based on Paul Irish' code, please read blogpost
//http://www.paulirish.com/2009/markup-based-unobtrusive-comprehensive-dom-ready-execution///
//Does 2 jobs:
//    * Page dependend execution of functions
//    * Gives a more fine-grained control in which order stuff is executed
//
//Adding a page dependend function: 
//    1. add class to body <body class="superPage"> 
//    2. add functions to the IFSLoader object IFSLoader = { superPage : init : function() {}};
//
//For now this will suffice, if complexity increases we might look at a more complex loader like requireJs. 
//Please think before adding javascript, this project should work without any of this scripts.  

var IFS = {
  common : {
    init : function(){
        ifs_modalLink.init();
        ifs_collapsible.init();
        ifs_wordCount.init();
    }, 
    finalize : function(){
        ifs_pieChart.init();
    }
  },
  'app-form' : {
    init : function(){
        ifs_unsavedChanges.init();
        ifs_autoSave.init();
        ifs_finance.init();
        ifs_financeRows.init();
    }
  },
  'app-details' : {
    init : function(){ ifs_application_page.init(); }
  },
  'assessment-details' : {
    init: function(){ ifs_assesment_feedback_page.init();}  
  },
  'assessment-submit-review' : {
    init: function(){ ifs_assesment_submit_review_page.init();}  
  }
};

