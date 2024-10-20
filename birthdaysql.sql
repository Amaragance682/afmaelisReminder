CREATE TABLE birthdays (
    id INTEGER NOT NULL PRIMARY KEY,
    name varchar(255),
    birthday varchar(255),
    age int
);



INSERT INTO birthdays (name, birthday, age)
VALUES ("Daniel", "15. september", 21);


INSERT INTO birthdays (name, birthday, age)
VALUES ("Oli", "18. nov", 22);

SELECT *
FROM birthdays;



DROP TABLE birthdays;