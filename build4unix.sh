#!/bin/bash
_this_dir=$(dirname $0)
mkdir "${_this_dir}/target"
(echo "#!/usr/bin/env java -jar"; cat "${_this_dir}"/target/tlscurl-*.jar) > "${_this_dir}/target/tlscurl"
chmod +x "${_this_dir}/target/tlscurl"
tar -zcvf "${_this_dir}/target/tlscurl_unix.tar.gz" "${_this_dir}/target/tlscurl"
