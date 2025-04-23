import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.*

/**
 * Обёртка для контекста, позволяющая динамически изменить язык приложения.
 *
 * Используется для локализации интерфейса в зависимости от выбранного пользователем языка.
 *
 * @constructor Принимает базовый [Context].
 */
class MyContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {

        /**
         * Оборачивает переданный контекст с применением нового языка.
         *
         * @param ctx Контекст, который нужно обернуть.
         * @param language Язык, который должен быть установлен (например, "ru", "en").
         * @return Новый [ContextWrapper] с обновлённой локалью.
         */
        @Suppress("DEPRECATION")
        fun wrap(ctx: Context, language: String): ContextWrapper {
            var context = ctx
            val config = context.resources.configuration
            val sysLocale: Locale? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }

            if (language != "" && sysLocale?.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
            }

            context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context.createConfigurationContext(config)
            } else {
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context
            }

            return MyContextWrapper(context)
        }

        /**
         * Получает текущую системную локаль на устаревших версиях Android.
         *
         * @param config Конфигурация ресурсов.
         * @return Текущая локаль.
         */
        @Suppress("DEPRECATION")
        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        /**
         * Получает текущую системную локаль на Android 7.0+.
         *
         * @param config Конфигурация ресурсов.
         * @return Текущая локаль.
         */
        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        /**
         * Устанавливает локаль в конфигурации на устаревших версиях Android.
         *
         * @param config Конфигурация, в которую применяется локаль.
         * @param locale Новая локаль.
         */
        @Suppress("DEPRECATION")
        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        /**
         * Устанавливает локаль в конфигурации на Android 7.0+.
         *
         * @param config Конфигурация, в которую применяется локаль.
         * @param locale Новая локаль.
         */
        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}
