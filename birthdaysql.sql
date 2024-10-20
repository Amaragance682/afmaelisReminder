CREATE TABLE Birthdays (
    id INTEGER NOT NULL PRIMARY KEY,
    name varchar(255),
    birthday varchar(255),
    age int
);



INSERT INTO Birthdays (name, birthday, age)
VALUES ("Daniel", "15. september", 21);


INSERT INTO Birthdays (name, birthday, age)
VALUES ("Oli", "18. nov", 22);

SELECT *
FROM Birthdays;



DROP TABLE Birthdays;