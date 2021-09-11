# ScreenChanger

ScreenChanger is a linux program that allows the user to automatically change their desktop background to a random image based on a query every hour. Images are taken from the Unsplash API.
 
## Setup

Download auto_changer.py. That's it!

## Usage

ScreenChanger can be ran with these commands:
```bash
cd *some directory with auto_changer.py*
python3 auto_changer.py [QUERY]
```

If no query is given, the images will be random from the Unsplash API.
ScreenChanger also automatically downloads the background to the given directory as well.


If the user would like to stop automated background changes, they may use: 

```bash
cd *some directory with auto_changer.py*
python3 auto_changer.py -d
```

## License
https://unsplash.com/license

## Note
Unsplash does not allow API keys to be publically published, so I have omitted the key from the source code.
I have only tested ScreenChanger on Ubuntu 20.04.

