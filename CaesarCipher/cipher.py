from random import *
import os
#Dictionary containing frequency of all letters in the English alphabet according to
#http://pi.math.cornell.edu/~mec/2003-2004/cryptography/subs/frequencies.html
frequencies = {'E': 12.02, 'T': 9.10, 'A': 8.12, 'O': 7.68, 'I': 7.31, 'N': 6.95, 'S': 6.28,
    'R': 6.02, 'H': 5.92, 'D': 4.32, 'L': 3.98, 'U': 2.88, 'C': 2.71, 'M': 2.61, 'F': 2.30,
    'Y': 2.11, 'W': 2.09, 'G': 2.03, 'P': 1.82, 'B': 1.49, 'V': 1.11, 'K': 0.69, 'X': 0.17, 'Q': 0.11,
    'J': 0.10, 'Z': 0.07}

############
#Encryption#
############

def encrypt(s, key):
    """Encrypts a string s according to an alphabet shift denoted by key"""
    start, end = ord('A'), ord('Z')
    rv = ''
    for char in s:
        if char.isalpha():
            char = char.upper()
            n = ord(char) + key
            if n > end:
                n = n % end + start - 1
            char = chr(n)
        rv += char
    return rv

def encrypt_file(filename):
    """Applies encryption function to all lines in a file"""
    #filename = os.getcwd() + '/' + filename
    f = open(filename, 'r')
    key = randint(0, 25)
    s = f.read()
    #for line in f:
    #    s += encrypt(line, key)
    return encrypt(s, key)

############
#Decryption#
############

def parse_file(filename):
    """Returns string from file"""
    f = open(filename, 'r')
    return f.read()

def freq_dict(s):
    """Returns frequency dictionary (i.e. dictionary with frequency of each letter) from string s"""
    d = init_dict()
    total = 0
    for c in s:
        if c.isalpha():
            d[c] += 1
    total = sum(d.values())
    for c in d:
        d[c] = (d[c]/total) * 100
    return d

def init_dict():
    """Initialized dictionary with key for each letter of alphabet"""
    d = {}
    s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for c in s:
        d[c] = 0
    return d

def chi_squared(observed, expected):
    """Returns chi-squared value for two dictionaries"""
    assert sorted(observed.keys()) == sorted(expected.keys()), 'Malformed dictionaries'
    total = 0
    for k in observed:
        actual = expected[k]
        diff = observed[k] - actual
        total += pow(diff, 2) / actual
    return total

def gen_possible_dict(encrypted, shift):
    """Generates one possible decryption dictionary given encryption dictionary and shift"""
    possible = {}
    for char in encrypted:
        possible[encrypt(char, shift)] = encrypted[char]
    return possible

def gen_all_dicts(encrypted):
    """Generates all possible decryption dictionaries"""
    return {n: gen_possible_dict(encrypted, n) for n in range(0, 26)}

def minimum(possible_dicts, orig_encrypt):
    """Finds the minimum chi-squared value for all possible decryption dictionaries, 
    then, decrypts the original message with this decryption dictionary"""
    m = min(possible_dicts.values(), key=lambda dictionary: chi_squared(dictionary, frequencies))
    for shift, s in possible_dicts.items():
        if m == s:
            return encrypt(orig_encrypt, shift)

def decrypt(encypted):
    """Returns decrypted string from encrypted string"""
    encrypt_dictionary = freq_dict(encypted)
    all_ds = gen_all_dicts(encrypt_dictionary)
    return minimum(all_ds, encypted)

def run(): 
    """Parse and use command-line arguments"""
    import argparse
    parser = argparse.ArgumentParser(description="Caesar Cipher Encoder/Decoder")
    parser.add_argument('option', type=int, help="0 for encrypt, 1 for decrypt")
    parser.add_argument('infile', help="Infile name")
    parser.add_argument('outfile', nargs='?', default='nonamegiven.txt', help="Outfile name")
    args = parser.parse_args()
    to_write = ''
    if args.option == 0:
        to_write = encrypt_file(args.infile)
    elif args.option == 1:
        plaintext = parse_file(args.infile)
        to_write = decrypt(plaintext)
        
    f = open(args.outfile, 'w')
    f.write(to_write)
    f.close()

run()