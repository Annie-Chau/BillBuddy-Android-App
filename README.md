# BillBuddy-Android-App
This is a group project for building an expense splitting app running on Android phones.

Team Members and Work Distribution:

Chau Nguyen - s3923010@rmit.edu.vn:
    Implemented the createExpense method and handle logic expense logic.
    Updated the real-time changes for UIs.
    Worked on the BalanceListAdapter.


Phuoc Dinh - s3878270@rmit.edu.vn:  
    Developed the user authentication and profile management features.
    Integrated Firebase Firestore for real-time database updates.
    Design UI/UX.
    Develop premium upgrade feature.
    
Hao Pham - s3891710@rmit.edu.vn:  
    Handle the chat feature.
    Worked on the ReimbursementAdapter to handle reimbursement logic.
    Assisted with testing and debugging the application.

Khuong Nguyen - s3924577@rmit.edu.vn:
    Handle the chat feature.
    Develop premium upgrade feature.
    Worked on the ReimbursementAdapter to handle reimbursement logic.
Functionalities:

Expense Management:  
    Create, update, and delete expenses.
    Automatically set and compare createdTime for expenses and reimbursements.
    Sort expenses by createdTime in descending order.
    
User Authentication:  
    User login and registration using Firebase Authentication.
    Profile management including profile picture upload.
    
Group Management:  
    Create and manage groups.
    Real-time updates for group changes.
    Filter and search groups based on various criteria.
    
Reimbursement Handling:  
    Track and display reimbursements within groups.
    Calculate and display user balances.
    
Premium Account Upgrade:  
    Upgrade to a premium account using Stripe for payment processing.
Technology Used: Languages: Java, Kotlin Frameworks: Android SDK Database: Firebase Firestore Authentication: Firebase Authentication Image Loading: Glide Payment Processing: Stripe, node.js

Open Issues and Known Bugs: Issue 1:
Description: Occasionally, the real-time updates for groups do not reflect immediately. Status: Under investigation. Issue 2:
Description: Profile pictures sometimes fail to load due to network issues. Status: Known bug, retry mechanism to be implemented. Issue 3:
Description: The BalanceListAdapter does not update correctly when reimbursements are modified. Status: Known bug, fix in progress.
