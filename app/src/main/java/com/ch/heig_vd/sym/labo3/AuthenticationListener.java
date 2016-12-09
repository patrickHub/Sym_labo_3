package com.ch.heig_vd.sym.labo3;

import java.util.EventListener;


public interface AuthenticationListener extends EventListener{
    void handleAuthentification (Integer level);
}
