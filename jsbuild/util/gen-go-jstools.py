#!/usr/bin/env python
"""Generate go-jstools.py"""
import sys
import textwrap
import virtualenv

filename = 'go-jstools.py'

after_install = """\
import os, subprocess
def after_install(options, home_dir):
    etc = join(home_dir, 'etc')
    ## TODO: this should all come from distutils
    ## like distutils.sysconfig.get_python_inc()
    if sys.platform == 'win32':
        lib_dir = join(home_dir, 'Lib')
        bin_dir = join(home_dir, 'Scripts')
    elif is_jython:
        lib_dir = join(home_dir, 'Lib')
        bin_dir = join(home_dir, 'bin')
    else:
        lib_dir = join(home_dir, 'lib', py_version)
        bin_dir = join(home_dir, 'bin')

    if not os.path.exists(etc):
        os.makedirs(etc)
    #subprocess.call([join(bin_dir, 'easy_install'),
    #    '--index-url', 'http://dev.camptocamp.com/packages/mapfish/%s/index',
    #    '--allow-hosts', 'dev.camptocamp.com',
    #    'JSTools'])
    subprocess.call([join(bin_dir, 'easy_install'),
         'http://github.com/bbinet/jstools/tarball/master'])
"""


def generate(filename, version):
    # what's commented out below comes from go-pylons.py

    #path = version
    #if '==' in version:
    #    path = version[:version.find('==')]
    #output = virtualenv.create_bootstrap_script(
    #    textwrap.dedent(after_install % (path, version)))

    output = virtualenv.create_bootstrap_script(
        textwrap.dedent(after_install % version))
    fp = open(filename, 'w')
    fp.write(output)
    fp.close()


def main():
    if len(sys.argv) != 2:
        print >> sys.stderr, 'usage: %s version' % sys.argv[0]
        sys.exit(1)
    generate(filename, sys.argv[1])


if __name__ == '__main__':
    main()
