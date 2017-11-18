
import toml

with open('config.toml') as fp:
    config = toml.load(fp)
    del fp
