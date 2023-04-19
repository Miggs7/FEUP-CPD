import sqlite3
import random
import string

def generate_username(length):
    """Generates a random username of specified length"""
    username = ''.join(random.choices(string.ascii_lowercase, k=length))
    return username

def generate_password(length):
    """Generates a random password of specified length"""
    password = ''.join(random.choices(string.ascii_letters + string.digits, k=length))
    return password

# Connect to the database
conn = sqlite3.connect('users.db')

# Create a table to store the username and password
conn.execute('CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)')

# Generate a random username and password
username = "admin"
password = "123456"

# Insert the username and password into the database
conn.execute('INSERT INTO users (username, password) VALUES (?, ?)', (username, password))
conn.commit()

# Retrieve the username and password from the database
result = conn.execute('SELECT username, password FROM users').fetchone()

# Print the username and password
print(f"Username: {result[0]} Password: {result[1]}")

#Close the connection
conn.close()