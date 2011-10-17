#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os, os.path
import sys
import subprocess
import shutil
import re

# Here you can create play commands that are specific to the module, and extend existing commands

MODULE = 'fap'

# Commands that are specific to your module

COMMANDS = ['fap:hello', 'fap:generate', 'fap:init', 'fap:version']
# Eliminamos el comando 'fap:model' de la lista de comandos

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
       
    if command == "fap:hello":
        print "~ Hello"

    if command == "fap:generate":
        run_generate(app, args)
        
#    if command == "fap:model":
#        run_model(app, args)
            
    if command == "fap:init":
        init_application (app, args)

    if command == "fap:version":
        version(app, args)



def version (app, args):
    depsYaml = os.path.join(getModuleDir(app), 'conf/dependencies.yml')
    if os.path.exists(depsYaml):
        deps = open(depsYaml).read()
        try:
             moduleDefinition = re.search(r'self:\s*(.*)\s*', deps).group(1)
             print moduleDefinition
        except Exception:
             pass
	
		
def execute_workflow(modelPath, targetPath, params, cmd_args, app):
    moduleDir = getModuleDir(app, cmd_args)

    if(moduleDir == None):
        print 'No se encontro la ruta del modulo'
        sys.exit()
    
    generatorDir = os.path.join(moduleDir, 'compiler')
    generatorLibDir = os.path.join(generatorDir, 'lib')
    jars = []
	

    #Librerías
    if("--dev" in cmd_args):	
        #Desarrollo
        jars.append(os.path.join(moduleDir, "compiler/lib/*")) 
        jars.append(os.path.join(moduleDir, "compiler/src/es.fap.simpleled.generator/bin/"))
        jars.append(os.path.join(moduleDir, "compiler/src/es.fap.simpleled/bin/"))
    else:
        #No desarrollo
        jars.append(os.path.join(moduleDir, "compiler" ,"lib/*")) 
        jars.append(os.path.join(moduleDir, "compiler", "compiled/*")) 
        
    #Variables para ejecutar el script	
    separador = ':'
    if os.name == 'nt':
        separador = ';'
    
    classpath = separador.join(str(x) for x in jars)

    if("--debug" in cmd_args):
        log = "file:///" + os.path.join(generatorDir, "config", "log4-debug.properties")
    else:
        log = "file:///" + os.path.join(generatorDir, "config", "log4.properties")
    
    os.chdir(generatorDir);
    class_name = "org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher"
	
    fapModelPath = os.path.join(moduleDir, "app", "led");
    workflow = "workflow.LedGenerator";
    
    cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath, "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, params];
    subprocess.call(cmd);
		
    

def run_generate(app, args):
    modelPath = os.path.join(app.path, "led")
    targetPath =  app.path + "/"