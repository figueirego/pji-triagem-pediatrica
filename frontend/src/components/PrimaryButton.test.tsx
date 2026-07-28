import { render } from '@testing-library/react-native';
import { StyleSheet } from 'react-native';
import { PrimaryButton } from './PrimaryButton';

describe('PrimaryButton', () => {
  it('keeps transform as an array when not pressed', async () => {
    const view = await render(<PrimaryButton>Entrar</PrimaryButton>);
    const pressable = view.getByRole('button');
    const style = StyleSheet.flatten(pressable.props.style);

    expect(style.transform).toEqual([]);
  });
});
