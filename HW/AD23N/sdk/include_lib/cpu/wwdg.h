#ifndef __WWDG_H__
#define __WWDG_H__

#include "typedef.h"


#define CONF_KEY    0xa6a0
#define RELOAD_KEY  0xa6a1

#define WWDG_SSEL(x)     SFR(JL_WWDG->CFG, 14, 2, x)
#define WWDG_PSEL(x)     SFR(JL_WWDG->CFG, 11, 3, x)
#define WWDG_EWI(x)      SFR(JL_WWDG->CFG, 9, 1, x)
#define WWDG_WIN(x)      SFR(JL_WWDG->CFG, 0, 7, x)
#define WWDG_PRD(x)      SFR(JL_WWDG->CR, 0, 7, x)
#define WWDG_EN(x)       SFR(JL_WWDG->CR, 7, 1, x)
#define WWDG_KEY(x)      JL_WWDG->KEY = x
#define WWDG_INT()       (JL_WWDG->SR & BIT(0))

typedef void (*cbfun)(void *priv);
void wwdg_reload();
void wwdg_init(void);
void wwdg_deinit(void);
void set_wwdg_callback(cbfun func);

#endif

