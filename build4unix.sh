#!/bin/bash
_this_dir=$(dirname $0)
(echo "#!/usr/bin/env java -jar"; cat "${_this_dir}"/target/tlscurl-*.jar) > tlscurl
chmod +x tlscurl
tar -zcvf tlscurl_unix.tar.gz tlscurl
