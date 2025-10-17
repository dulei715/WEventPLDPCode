#!/bin/bash
class_name="Log"

#totalMem="-Xms120g"
#maxMem="-Xmx2400g"
java ${totalMem} ${maxMem} -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar hnu.dll.run.d_total_run._5_main_run.post_process.${class_name}MainLDPPosterRun