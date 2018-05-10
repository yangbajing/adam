#!/usr/bin/env bash

wrk -t2 -c256 -d60 -T5 \
                --script=./scripts/wrk.lua \
                --latency http://127.0.0.1:8087/invoke
