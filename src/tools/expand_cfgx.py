#!/usr/bin/env python

import argparse
import sys

START_SYMBOL = 'Goal'
OPT_TAG = '(opt)'

parser = argparse.ArgumentParser()
parser.add_argument('input_file', help='Path to input .cfgx file')
parser.add_argument('output_file', help='Path to output .cfg file')

args = parser.parse_args()
input_file = open(args.input_file)
output_file = open(args.output_file, 'w')

terminals = set()
nonterminals = set()
rules = []
opts = set()

# Read nonterminals
num_terminals = int(input_file.readline())
for i in xrange(num_terminals):
    terminal = input_file.readline().strip()
    terminals.add(terminal)

# Read and process rules
line = input_file.readline()
nonterminal = None
while len(line) > 0:
    line = line.strip()
    if len(line) > 0 and line[0] != '#':
        is_nonterminal = line[-1] == ':'
        if is_nonterminal:
            nonterminal = line[:-1]
            nonterminals.add(nonterminal)
        else:
            if nonterminal == None:
                sys.exit("Nonterminal expected when starting a rule")
            symbols = line.split()
            rule = [nonterminal]
            for symbol in symbols:
                if symbol[-len(OPT_TAG):] == OPT_TAG:
                    opts.add(symbol)
                if not symbol in terminals:
                    nonterminals.add(symbol)
                rule.append(symbol)
            rules.append(rule)
    line = input_file.readline()

# Generate rules for (opt) nonterminals
for opt in opts:
    rules.append([opt])
    rules.append([opt, opt[:-len(OPT_TAG)]])

# TODO Check consistency of rules (e.g. make sure every nonterminal has a rule)

print "Outputting new cfg file with {} terminals, {} nonterminals, and {} rules".format(len(terminals), len(nonterminals), len(rules))
output_file.write(str(len(terminals)) + '\n')
for terminal in terminals:
    output_file.write(terminal + '\n')
output_file.write(str(len(nonterminals)) + '\n')
for nonterminal in nonterminals:
    output_file.write(nonterminal + '\n')
output_file.write(START_SYMBOL + '\n')
output_file.write(str(len(rules)) + '\n')
for rule in rules:
    output_file.write(' '.join(rule) + '\n')
