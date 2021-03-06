CREATE DATABASE IF NOT EXISTS proj2016;
USE proj2016;

/*
DROP TABLE IF EXISTS Alert;
DROP TABLE IF EXISTS Auction;
DROP TABLE IF EXISTS AutoBid;
DROP TABLE IF EXISTS Bid;
DROP TABLE IF EXISTS Email;
DROP TABLE IF EXISTS Question;
DROP TABLE IF EXISTS Wishlist;
DROP TABLE IF EXISTS Item;
DROP TABLE IF EXISTS User;
*/

CREATE TABLE User(
	Address varchar(255) DEFAULT NULL,
	BirthDate date DEFAULT NULL,
	City varchar(255) DEFAULT NULL,
	Country varchar(255) DEFAULT NULL, /* Full country name, e.g. Canada instead of CA */
	Email varchar(255) DEFAULT NULL,
	FirstName varchar(255) DEFAULT NULL,
	LastName varchar(255) DEFAULT NULL,
	Password varchar(255) NOT NULL,
	Phone varchar(12) DEFAULT NULL,
	PostCode varchar(9) DEFAULT NULL,
	Role varchar(255) NOT NULL, /* EndUser for reg users, admin for admins, rep for customer reps*/
	State varchar(255) DEFAULT NULL, /* Full state name, e.g. New Jersey instead of NJ */
	UserID int(9) NOT NULL AUTO_INCREMENT,
	Username varchar(255) NOT NULL,
	PRIMARY KEY(UserID)
);

INSERT INTO User VALUES(NULL,NULL,NULL,NULL,"system@bouncehouseemporium",NULL,NULL,"password",NULL,NULL,"admin", NULL,"1","system");
INSERT INTO User VALUES(NULL, NULL, NULL, NULL, 'tgoetjen@gmail.com', 'Tim','Goetjen', 'password', NULL, 
	NULL,'admin',NULL,2,'tgoetjen');
INSERT INTO User VALUES(NULL, NULL, NULL, NULL, 'kinhhoang@gmail.com', 'Kinh','Hoang', 'hoang', NULL, 
	NULL,'rep',NULL,3,'kinh');

CREATE TABLE Item(
	Bounciness int(2) DEFAULT 0,
	Category varchar(255) DEFAULT NULL,
	Description varchar(3000) DEFAULT NULL,
	ItemID int(9) NOT NULL AUTO_INCREMENT,
	Size varchar(2) DEFAULT NULL,
	SubCategory varchar(255) DEFAULT NULL,
	Title varchar(255) DEFAULT NULL,
	PRIMARY KEY(ItemID)
);

CREATE TABLE Auction(
	AuctionID int(9) NOT NULL AUTO_INCREMENT,
	BidIncrement int(2) DEFAULT 1,
	CloseDate datetime DEFAULT NULL,
	Completed int(1) DEFAULT 0,
	ItemID int(9) NOT NULL,
	MinBid float(7) DEFAULT 0,
	UserID int(9) NOT NULL,
	WinBid float(7) DEFAULT NULL,
	WinnerID int(9) DEFAULT NULL,
	FOREIGN KEY (WinnerID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (ItemID) REFERENCES Item (ItemID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY(AuctionID)
);

CREATE TABLE Alert(
	AlertID int(9) NOT NULL AUTO_INCREMENT,
	ItemID int(9) NOT NULL,
	UserID int(9) NOT NULL,
	FOREIGN KEY (ItemID) REFERENCES Item (ItemID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY(AlertID)
);

CREATE TABLE AutoBid(
	AutoBidID int(9) NOT NULL AUTO_INCREMENT,
	AuctionID int(9) NOT NULL,
	UserID int(9) NOT NULL,
	MaxBid float(7) DEFAULT 0,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (AuctionID) REFERENCES Auction (AuctionID) ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY(AutoBidID)
);

CREATE TABLE Bid(
	Amount float(7) DEFAULT 0,
	AuctionID int(9) NOT NULL,
	BidID int(9) NOT NULL AUTO_INCREMENT,
	BidTime datetime DEFAULT NULL,
	UserID int(9) NOT NULL,
	FOREIGN KEY (AuctionID) REFERENCES Auction (AuctionID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY(BidID)
);

/* 
	NULLS are allowed in this table only so that a deletion from "User" won't break a record -
	we do not want to cascade a delete (we lose records of our communication) or take no action 
	(user should be able to delete his or her account from our database at any point). The answer 
	to this is setting NULL instead. We keep our info, while the user's account is deleted.
*/ 
CREATE TABLE Email(
	Content varchar(8000) DEFAULT NULL,
	EmailID int(9) NOT NULL AUTO_INCREMENT,
	Recipient varchar(255) NOT NULL,
	RecipientID int(9) DEFAULT NULL, 
	Sender varchar(255) NOT NULL,
	SenderID int(9) DEFAULT NULL,
	SendTime datetime DEFAULT NULL,
	FOREIGN KEY (RecipientID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE SET NULL,
	FOREIGN KEY (SenderID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE SET NULL,
	PRIMARY KEY (EmailID)
);

/*
	See the comment on the email table for why it's better to set NULL on deletion of a user's
	account from the "User" table. The same principle applies to the customer rep - in short,
	we lose a record of communication between us and the user if we cascade a delete of the rep
	or the user, and if we specify NO ACTION, the database will reject the deletes, which is
	also unacceptable.
*/
CREATE TABLE Question(
	Answer varchar(8000) DEFAULT NULL,
	QuestionID int(9) NOT NULL AUTO_INCREMENT,
	QText varchar(8000) DEFAULT NULL,
	RepID int(9) DEFAULT NULL,
	Topic varchar(255) NOT NULL,
	UserID int(9) DEFAULT NULL,
	FOREIGN KEY (RepID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE SET NULL,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE SET NULL,
	PRIMARY KEY (QuestionID)
);

CREATE TABLE Wishlist(
	Bounciness int(2) DEFAULT 0, /* On a scale of {1, 2, ... 10} */
	Category varchar(255) DEFAULT NULL,
	ListID int(9) NOT NULL AUTO_INCREMENT,
	Size varchar(2) DEFAULT NULL,
	SubCategory varchar(255) DEFAULT NULL,
	UserID int(9) NOT NULL,
	FOREIGN KEY (UserID) REFERENCES User (UserID) ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY(ListID)
);

DELIMITER !

DROP TRIGGER IF EXISTS welcomeEmail!

CREATE TRIGGER welcomeEmail AFTER INSERT ON User
FOR EACH ROW BEGIN
	INSERT INTO Email(Content,Recipient,RecipientID,Sender,SenderID,SendTime) VALUES("Congratulations on making an account with Bouncehouse Emporium! We hope you'll find exactly what you're looking for. Please let us know if you need anything!",NEW.Email,NEW.UserID,"system@bouncehouseemporium","1", NOW());
END!

DROP TRIGGER IF EXISTS insertAlert!

CREATE TRIGGER insertAlert AFTER INSERT ON Item
FOR EACH ROW BEGIN
	IF (NEW.Bounciness IN (SELECT Bounciness FROM Wishlist) AND NEW.Category IN (SELECT Category FROM Wishlist) AND NEW.Size IN (Select Size FROM Wishlist) AND New.SubCategory IN (Select SubCategory FROM Wishlist)) THEN
		INSERT INTO Alert(ItemID, UserID) SELECT DISTINCT NEW.ItemID, UserID FROM Wishlist WHERE Bounciness = NEW.Bounciness AND Category = NEW.Category AND Size = NEW.Size AND SubCategory = NEW.SubCategory;
	END IF;
END!

DROP TRIGGER IF EXISTS alertRepOnNewQuestion!

CREATE TRIGGER alertRepOnNewQuestion AFTER INSERT ON Question
FOR EACH ROW BEGIN
		INSERT INTO Email(Content,Recipient,RecipientID,Sender,SenderID,SendTime) VALUES ("New question posted! You are the responding representative.", (SELECT Email FROM User WHERE UserID = NEW.RepID), NEW.RepID, "system@bouncehouseemporium", "1", NOW());
END!

DELIMITER ;
