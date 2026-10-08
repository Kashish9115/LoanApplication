CREATE DATABASE IF NOT EXISTS LoanApp;
USE LoanApp;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS SupportTickets;
DROP TABLE IF EXISTS KycDocuments;
DROP TABLE IF EXISTS LoanAccounts;
DROP TABLE IF EXISTS LoanDeals;
DROP TABLE IF EXISTS Customers;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Customers (
    CustomerId INT NOT NULL AUTO_INCREMENT,
    FirstName VARCHAR(100) NOT NULL,
    LastName VARCHAR(100),
    Email VARCHAR(255) NOT NULL,
    Password VARCHAR(255),
    MobileNo VARCHAR(20),
    AadhaarNo VARCHAR(30),
    EmploymentType VARCHAR(100),
    MonthlyIncome DECIMAL(18,2),
    IsEmailVerified BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (CustomerId),
    UNIQUE KEY UX_Customers_Email (Email)
) ENGINE=InnoDB;

CREATE TABLE KycDocuments (
    DocumentId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    DocumentType VARCHAR(100) NOT NULL,
    FilePath VARCHAR(1000),
    VerificationStatus VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (DocumentId),
    UNIQUE KEY UX_KycDocuments_Customer_Type (CustomerId, DocumentType),
    KEY IX_KycDocuments_CustomerId (CustomerId),
    CONSTRAINT FK_KycDocuments_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
) ENGINE=InnoDB;

CREATE TABLE LoanDeals (
    LoanDealId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    LoanType VARCHAR(100),
    LoanAmount DECIMAL(18,2),
    Status VARCHAR(50),
    PRIMARY KEY (LoanDealId),
    KEY IX_LoanDeals_CustomerId (CustomerId),
    CONSTRAINT FK_LoanDeals_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
) ENGINE=InnoDB;

CREATE TABLE LoanAccounts (
    LoanAccountId INT NOT NULL AUTO_INCREMENT,
    LoanDealId INT NOT NULL,
    CustomerId INT NOT NULL,
    AccountNumber VARCHAR(100) NOT NULL,
    PrincipalAmount DECIMAL(18,2),
    Status VARCHAR(50),
    PRIMARY KEY (LoanAccountId),
    UNIQUE KEY UX_LoanAccounts_AccountNumber (AccountNumber),
    KEY IX_LoanAccounts_CustomerId (CustomerId),
    CONSTRAINT FK_LoanAccounts_LoanDeals
        FOREIGN KEY (LoanDealId) REFERENCES LoanDeals(LoanDealId),
    CONSTRAINT FK_LoanAccounts_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
) ENGINE=InnoDB;

CREATE TABLE SupportTickets (
    TicketId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    LoanAccountId INT NULL,
    Subject VARCHAR(255) NOT NULL,
    Description VARCHAR(2000) NOT NULL,
    Status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UpdatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (TicketId),
    KEY IX_SupportTickets_CustomerId (CustomerId),
    KEY IX_SupportTickets_LoanAccountId (LoanAccountId),
    CONSTRAINT FK_SupportTickets_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId),
    CONSTRAINT FK_SupportTickets_LoanAccounts
        FOREIGN KEY (LoanAccountId) REFERENCES LoanAccounts(LoanAccountId)
) ENGINE=InnoDB;

INSERT INTO Customers
(CustomerId, FirstName, LastName, Email, Password, MobileNo, AadhaarNo, EmploymentType, MonthlyIncome, IsEmailVerified)
VALUES
(1, 'Rohit', 'Rakshe', 'rohit@test.com', 'test123', '9999999999', '123456789012', 'SALARIED', 60000.00, TRUE);

INSERT INTO LoanDeals (LoanDealId, CustomerId, LoanType, LoanAmount, Status)
VALUES (1, 1, 'PERSONAL_LOAN', 500000.00, 'APPROVED');

INSERT INTO LoanAccounts (LoanAccountId, LoanDealId, CustomerId, AccountNumber, PrincipalAmount, Status)
VALUES (1, 1, 1, 'LN100001', 500000.00, 'ACTIVE');

INSERT INTO KycDocuments (CustomerId, DocumentType, FilePath, VerificationStatus) VALUES
(1, 'PAN', 'uploads/kyc/pan_1.pdf', 'PENDING'),
(1, 'AADHAAR', 'uploads/kyc/aadhaar_1.pdf', 'PENDING'),
(1, 'SALARY_SLIP', 'uploads/kyc/salary_slip_1.pdf', 'PENDING');

INSERT INTO SupportTickets (CustomerId, LoanAccountId, Subject, Description, Status)
VALUES
(1, 1, 'EMI Issue', 'Customer wants help regarding EMI.', 'OPEN'),
(1, NULL, 'KYC Issue', 'Customer needs help regarding KYC.', 'OPEN');
