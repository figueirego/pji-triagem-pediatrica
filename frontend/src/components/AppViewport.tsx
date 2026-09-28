import type { ReactNode } from 'react';
import { Platform, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { colors } from '../theme';

/** Applies device safe bounds once, above both authentication and main screens. */
export function AppViewport({ children }: { children: ReactNode }) {
  const insets = useSafeAreaInsets();
  return (
    <View
      testID="app-safe-viewport"
      style={{
        alignItems: Platform.OS === 'web' ? 'center' : 'stretch',
        backgroundColor: colors.background,
        flex: 1,
        paddingTop: insets.top,
        paddingBottom: insets.bottom,
        paddingLeft: insets.left,
        paddingRight: insets.right,
      }}
    >
      <View style={{ flex: 1, maxWidth: Platform.OS === 'web' ? 430 : undefined, width: '100%' }}>
        {children}
      </View>
    </View>
  );
}
