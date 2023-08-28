# Translation

Translations are listed alongside english markdown:

* `example.md`
* `example.fr.md`

Using ***pandoc*** to convert to `html`, and then using the [Deepl REST API](http://deepl.com), 

```
pip3 install -r translate/requirements.txt
python3 -m translate
```

Provide environmental variable with Deepl authentication key:
```
export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
```

Translate using pandoc and deepl:
```
python3 -m translate french docs/example.md
```

See ``python3 -m translate --help`` for more options.

You are welcome to use  google translate, ChatGPT, or Deepl directly - keeping in mind markdown formatting may be lost.

