import sqlite3
from sqlite3 import Error

def initialize():
    # allt sem á að gerast einu sinni þegar við kveikjum á forritinu
    # t.d. tengingar milli forrita


    #Tengja db
    con = None
    try:
        con = sqlite3.connect("birthday.db")
    except Error as e:
        print(e)

    #stillum cursor
    cur = con.cursor()


    #stillum rust tengingu hér


    #skellum öllu inn í tuple til að forðast að senda 50 hluti áfram
    use = (con, cur)

    main(use)



def main(use):
    # Hér sitjum við þegar búið er að gera initialize, og förum héðan yfir í þau föll sem við þurfum
    info = getInfo()

    with use[0]:
        addToDatabase(use, info)




def getInfo():
    #temp, munum fá þessi gildi frá rust
    a = str(input("Nafn: "))
    b = str(input("Dagsetning: "))
    c = str(input("Aldur: "))
    return (a, b, c)


def addToDatabase(use, info):

    sql = '''INSERT INTO Birthdays(name, birthday, age)
            Values(?, ?, ?)'''
    use[1].execute(sql, info)
    use[0].commit()



def removeFromDatabase(nafn):
    return



def showDatabase():
    return
    # sýnum allt database


def searchDatabase(nafn):
    return
    # leita eftir nafni í database, sýna allar upplýsingar sem fylgja því


initialize()