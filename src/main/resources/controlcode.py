from pybricks.pupdevices import Motor
from pybricks.parameters import Port
from pybricks.tools import wait
from pybricks.pupdevices import UltrasonicSensor,ColorSensor
from pybricks.hubs import InventorHub
from pybricks.parameters import Stop


motor = Motor(Port.D)
motor1 = Motor(Port.C)

hub = InventorHub()
usens = UltrasonicSensor(Port.A)
csens = ColorSensor(Port.B)
smotor = Motor(Port.F)
sradmotor = Motor(Port.E)


motor.settings(9000)

VOLTAGE_FULL = 9000 # 7.3V (max voltage of your battery pack)
VOLTAGE_EMPTY = 4800  # 4.8V (typical hub shutdown voltage)
VOLTAGE_LOW_THRESHOLD = 5100  # ~5.1V, roughly 10% battery

voltage = hub.battery.voltage()  # Get voltage in millivolts
# Calculate percentage (linear approximation)
percentage = max(0, min(100, ((voltage - VOLTAGE_EMPTY) / (VOLTAGE_FULL - VOLTAGE_EMPTY)) * 100))

fullSpeed = 100

def move(speed):
    motor.dc(speed)
    motor1.dc(speed)

def movebrake():
    motor.brake()
    motor1.brake()

def throw():
    sradmotor.run_time(-500,800,Stop.BRAKE,False)
    wait(800)
    smotor.run_time(-150,1000,Stop.BRAKE,False)
    wait(500)
    sradmotor.run_time(500,800,Stop.BRAKE,False)
    wait(500)
    smotor.run_time(150,1000,Stop.BRAKE,True)

def checkField(index):
    sradmotor.run_angle(-500,index*65,Stop.BRAKE,wait=True)
    col = csens.hsv().v
    isred = False
    if (col >= 40 and col <= 60):
        isred = True
    sradmotor.run_angle(500,index*69,Stop.BRAKE,wait=True)
    return isred

def fahrzuSlow(row,ff):
    wanted = 55
    if (row == 0): wanted = 92
    if (row == 1): wanted = 120
    if (row == 2): wanted = 149
    if (row == 3): wanted = 179
    if (row == 4): wanted = 210
    if (row == 5): wanted = 231
    if (row == 6): wanted = 280

    dc = 0
    while True:
        distance = usens.distance()
        load = motor1.load() + motor.load()
        if load > 0:
            load = 0
        speed = fullSpeed - 50 - ((30/210)*load)
        print("R:",row,wanted,"±",ff,":",speed ,":",load,":"," -> ",distance)

        if (distance -ff) < wanted and (distance +ff) > wanted:
            print(wanted,":",distance)
            movebrake()
            break
        if distance == 2000:
            move(speed)
            wait(150)
            motor.dc(-speed)
        if distance < (wanted - ff):
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        else:
            speed = -speed
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        wait(10)


def fahrzu(row,ff):
    wanted = 55
    if (row == 0): wanted = 92
    if (row == 1): wanted = 120
    if (row == 2): wanted = 149
    if (row == 3): wanted = 179
    if (row == 4): wanted = 210
    if (row == 5): wanted = 231
    if (row == 6): wanted = 280

    dc = 0
    while True:
        distance = usens.distance()
        load = motor1.load() + motor.load()
        if load > 0:
            load = 0
        speed = fullSpeed - 30 - ((30/210)*load)
        print("R:",row,wanted,"±",ff,":",speed ,":",load,":"," -> ",distance)

        if (distance -ff) < wanted and (distance +ff) > wanted:
            print(wanted,":",distance)
            movebrake()
            break
        if distance == 2000:
            move(speed)
            wait(150)
            motor.dc(-speed)
        if distance < (wanted - ff):
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        else:
            speed = -speed
            if dc != speed:
                movebrake()
                move(speed)
                dc = speed
        wait(10)

def werf(row,ff,fieldIndex):
    checkAusrichtung()
    fahrzu(row,ff)
    print(usens.distance())
    throw()
    while(not checkField(fieldIndex)):
        shukle(row,ff)


def shukle(row,ff):
    move(70)
    wait(200)
    movebrake()
    fahrzuSlow(row,ff)
    move(-70)
    wait(200)
    movebrake()
    fahrzuSlow(row,ff)

def printDistance():
    while(True):
        print(usens.distance())

def checkAusrichtung():
    movebrake()
    move(-80)
    while(True):
        if (motor.stalled()):
            break
    movebrake()
    dist = usens.distance()
    if (dist >= 55 and dist <= 58):
        print("Ausrichtung richtig " , dist)
    else:
        print("Ausrichtung falsch " , dist)
        wait(500)
        checkAusrichtung()




print(voltage)
print(usens.distance())


index = 4
werf(4,1,index)
werf(0,1,index)
werf(2,1,index)
werf(5,1,index)
werf(3,1,index)
werf(1,1,index)
werf(6,10,index)
checkAusrichtung()


#fahrzu(-1,1)
#throw()


printDistance()

