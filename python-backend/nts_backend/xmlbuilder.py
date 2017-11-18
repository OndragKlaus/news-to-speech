"""
Because lxml and ElementTree suck, especially when you need to define
namespaces.
"""

import cgi
import io


class Node(object):

    def __init__(self, nodeName, *children_or_attrs, text=None):
        if text and nodeName != '#text':
            raise ValueError('text can only be defined on a #text node')

        self.nodeName = nodeName
        self.text = text
        self.children = []
        self.attrs = {}
        for item in children_or_attrs:
            if isinstance(item, dict):
                self.attrs.update(item)
            else:
                self.children.append(item)

    def tostring(self):
        fp = io.StringIO()
        self.tofile(fp)
        return fp.getvalue()

    def tofile(self, fp):
        if self.nodeName == '#text':
            fp.write(escape(self.text or ''))
        else:
            fp.write('<{}'.format(self.nodeName))
            if self.attrs:
                fp.write(' ')
                fp.write(' '.join(
                    '{}="{}"'.format(k, escape(v).replace('"', '\\"'))
                    for k, v in self.attrs.items()
                ))
            fp.write('>' if self.children else '/>')
            for child in self.children:
                child.tofile(fp)
            if self.children:
                fp.write('</{}>'.format(self.nodeName))


class NodeBuilder(object):

    def text(self, text):
        return Node('#text', text=text)

    def __getattr__(self, nodeName):
        def wrapper(*children_or_attrs):
            node = Node(nodeName, *children_or_attrs)
            return node
        return wrapper


def escape(s):
    # We need to escape special characters (eg. umlaute) for the
    # Text-to-Speech API, but neither cgi.escape() nor
    # xml.sax.saxutils.escape() does that.
    s = cgi.escape(s)
    s = ''.join('&#{};'.format(ord(c)) if ord(c) > 127 else c for c in s)
    return s


X = NodeBuilder()
