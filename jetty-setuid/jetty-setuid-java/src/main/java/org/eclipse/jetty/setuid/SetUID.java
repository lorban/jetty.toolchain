//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.setuid;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Class is for changing user and groupId, it can also be use to retrieve user information by using getpwuid(uid) or getpwnam(username) of both linux and unix
 * systems
 */

public class SetUID
{
    public static int setumask(int mask){
        return LibC.INSTANCE.setumask(mask);
    }

    public static int setuid(int uid){
        return LibC.INSTANCE.setuid(uid);
    }

    public static int setgid(int gid){
        return LibC.INSTANCE.setgid(gid);
    }

    public static int setgroups(int[] gids){
        return LibC.INSTANCE.setgroups(gids.length, gids);
    }

    public static Passwd getpwnam(String name) throws SecurityException{
        passwd ptr = LibC.INSTANCE.getpwnam(name);
        if (ptr == null)
            return null;
        return new Passwd(ptr.pw_name, ptr.pw_passwd, ptr.pw_uid, ptr.pw_gid, ptr.pw_gecos, ptr.pw_dir, ptr.pw_shell);
    }

    public static Passwd getpwuid(int uid) throws SecurityException{
        passwd ptr = LibC.INSTANCE.getpwuid(uid);
        if (ptr == null)
            return null;
        return new Passwd(ptr.pw_name, ptr.pw_passwd, ptr.pw_uid, ptr.pw_gid, ptr.pw_gecos, ptr.pw_dir, ptr.pw_shell);
    }

    public static Group getgrnam(String name) throws SecurityException{
        group ptr = LibC.INSTANCE.getgrnam(name);
        if (ptr == null)
            return null;
        return new Group(ptr.gr_name, ptr.gr_passwd, ptr.gr_gid, ptr.gr_mem.getStringArray(0));
    }

    public static Group getgrgid(int gid) throws SecurityException{
        group ptr = LibC.INSTANCE.getgrgid(gid);
        if (ptr == null)
            return null;
        return new Group(ptr.gr_name, ptr.gr_passwd, ptr.gr_gid, ptr.gr_mem.getStringArray(0));
    }

    public static RLimit getrlimitnofiles(){
        rlimit rlim = new rlimit();
        int rc = LibC.INSTANCE.getrlimit(LibC.RLIMIT_NOFILE, rlim);
        if (rc == -1)
            return null;
        return new RLimit(rlim.rlim_cur, rlim.rlim_max);
    }

    public static int setrlimitnofiles(RLimit rlimit) {
        rlimit rlim = new rlimit();
        rlim.rlim_cur = rlimit._soft;
        rlim.rlim_max = rlimit._hard;
        return LibC.INSTANCE.setrlimit(LibC.RLIMIT_NOFILE, rlim);
    }


    private interface LibC extends Library
    {
        LibC INSTANCE = Native.load((Platform.isWindows() ? "msvcrt" : "c"), LibC.class);

        int setumask(int mask);

        int setuid(int uid);

        int setgid(int gid);

        int setgroups(int size, int[] gids);

        passwd getpwnam(String name);

        passwd getpwuid(int uid);

        group getgrnam(String name);

        group getgrgid(int gid);

        int RLIMIT_NOFILE = 7;       /* max number of open files */

        int getrlimit(int resource, rlimit rlim);

        int setrlimit(int resource, rlimit rlim);
    }

    public static class passwd extends Structure
    {
        public String pw_name;
        public String pw_passwd;
        public int pw_uid;
        public int pw_gid;
        public String pw_gecos;
        public String pw_dir;
        public String pw_shell;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pw_name", "pw_passwd", "pw_uid", "pw_gid", "pw_gecos", "pw_dir", "pw_shell");
        }
    }

    public static class group extends Structure
    {
        public String gr_name;
        public String gr_passwd;
        public int gr_gid;
        public Pointer gr_mem;

        @Override
        protected List<String> getFieldOrder()
        {
            return Arrays.asList("gr_name", "gr_passwd", "gr_gid", "gr_mem");
        }
    }

    public static class rlimit extends Structure
    {
        public int rlim_cur;
        public int rlim_max;

        @Override
        protected List<String> getFieldOrder()
        {
            return Arrays.asList("rlim_cur", "rlim_max");
        }
    }
}
