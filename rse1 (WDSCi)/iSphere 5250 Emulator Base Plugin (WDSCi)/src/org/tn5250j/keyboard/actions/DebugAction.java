/**
 * Title: DebugAction.java
 * Copyright:   Copyright (c) 2001,2002
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.keyboard.actions;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.SessionGUI;
import org.tn5250j.keyboard.KeyMapper;

/**
 * Display session attributes
 */
public class DebugAction extends EmulatorAction implements TN5250jConstants {

    public DebugAction(SessionGUI session, KeyMapper keyMap) {
        super(session, MNEMONIC_DEBUG, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK), keyMap);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        session.toggleDebug();
    }
}